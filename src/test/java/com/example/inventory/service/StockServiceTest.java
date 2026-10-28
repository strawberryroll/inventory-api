package com.example.inventory.service;

import com.example.inventory.domain.Product;
import com.example.inventory.domain.StockHistory;
import com.example.inventory.domain.StockType;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.StockHistoryResponse;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

// Mockito로 Repository를 가짜(mock)로 바꿔 DB 없이 Service의 판단 로직만 검증한다.
// 주의: DB를 쓰지 않으므로 @Transactional 롤백이나 SQL이 올바른지는 여기서 확인할 수 없다. (통합 테스트에서 확인)
@ExtendWith(MockitoExtension.class)
class StockServiceTest {

	// @Mock: 진짜 대신 쓸 가짜 객체. 우리가 정한 대로만 응답한다.
	@Mock
	private ProductRepository productRepository;

	@Mock
	private StockHistoryRepository stockHistoryRepository;

	// @InjectMocks: 위의 가짜 객체들을 생성자에 넣어 StockService를 만들어 준다.
	@InjectMocks
	private StockService stockService;

	@Test
	@DisplayName("입고 성공: 재고가 늘고 IN 이력이 저장된다")
	void stockIn_success() {
		Product product = new Product("P-001", "볼트 M8", 500, 10);
		given(productRepository.findById(1L)).willReturn(Optional.of(product));

		ProductResponse response = stockService.stockIn(1L, 30);

		assertThat(response.stockQuantity()).isEqualTo(40);

		// 가짜 Repository에 어떤 이력이 저장되도록 전달됐는지 꺼내서 확인한다.
		ArgumentCaptor<StockHistory> captor = ArgumentCaptor.forClass(StockHistory.class);
		verify(stockHistoryRepository).save(captor.capture());
		StockHistory saved = captor.getValue();
		assertThat(saved.getType()).isEqualTo(StockType.IN);
		assertThat(saved.getQuantity()).isEqualTo(30);
		assertThat(saved.getProduct()).isSameAs(product);
	}

	@Test
	@DisplayName("출고 성공: 재고가 줄고 OUT 이력이 저장된다")
	void stockOut_success() {
		Product product = new Product("P-001", "볼트 M8", 500, 10);
		given(productRepository.findById(1L)).willReturn(Optional.of(product));

		ProductResponse response = stockService.stockOut(1L, 4);

		assertThat(response.stockQuantity()).isEqualTo(6);

		ArgumentCaptor<StockHistory> captor = ArgumentCaptor.forClass(StockHistory.class);
		verify(stockHistoryRepository).save(captor.capture());
		assertThat(captor.getValue().getType()).isEqualTo(StockType.OUT);
		assertThat(captor.getValue().getQuantity()).isEqualTo(4);
	}

	@Test
	@DisplayName("출고 재고 부족: 예외가 나고 이력은 저장되지 않으며 재고도 그대로다")
	void stockOut_insufficientStock_doesNotSaveHistory() {
		Product product = new Product("P-001", "볼트 M8", 500, 10);
		given(productRepository.findById(1L)).willReturn(Optional.of(product));

		assertThatThrownBy(() -> stockService.stockOut(1L, 20))
				.isInstanceOf(InsufficientStockException.class);

		// "저장이 한 번도 호출되지 않았다"를 검증한다. 재고 음수 방지가 이력까지 막는다는 증거.
		verify(stockHistoryRepository, never()).save(any());
		assertThat(product.getStockQuantity()).isEqualTo(10);
	}

	@Test
	@DisplayName("입고: 없는 상품이면 예외가 나고 이력은 저장되지 않는다")
	void stockIn_productNotFound_throws() {
		given(productRepository.findById(999L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> stockService.stockIn(999L, 10))
				.isInstanceOf(ProductNotFoundException.class);

		verify(stockHistoryRepository, never()).save(any());
	}

	@Test
	@DisplayName("출고: 없는 상품이면 예외가 나고 이력은 저장되지 않는다")
	void stockOut_productNotFound_throws() {
		given(productRepository.findById(999L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> stockService.stockOut(999L, 10))
				.isInstanceOf(ProductNotFoundException.class);

		verify(stockHistoryRepository, never()).save(any());
	}

	@Test
	@DisplayName("이력 조회: 없는 상품이면 예외가 나고 이력 조회는 호출되지 않는다")
	void getStockHistories_productNotFound_throws() {
		given(productRepository.existsById(999L)).willReturn(false);

		assertThatThrownBy(() -> stockService.getStockHistories(999L))
				.isInstanceOf(ProductNotFoundException.class);

		verify(stockHistoryRepository, never()).findByProductIdOrderByCreatedAtDesc(anyLong());
	}

	@Test
	@DisplayName("이력 조회: productId가 담긴 응답으로 변환된다")
	void getStockHistories_success() {
		Product product = new Product("P-001", "볼트 M8", 500, 10);
		// id는 DB가 채워 주는 값이라 테스트에서는 리플렉션으로 직접 넣는다.
		ReflectionTestUtils.setField(product, "id", 1L);
		given(productRepository.existsById(1L)).willReturn(true);
		given(stockHistoryRepository.findByProductIdOrderByCreatedAtDesc(1L)).willReturn(List.of(
				new StockHistory(product, StockType.OUT, 15),
				new StockHistory(product, StockType.IN, 30)
		));

		List<StockHistoryResponse> result = stockService.getStockHistories(1L);

		assertThat(result).hasSize(2);
		assertThat(result.get(0).type()).isEqualTo(StockType.OUT);
		assertThat(result.get(0).quantity()).isEqualTo(15);
		assertThat(result.get(1).type()).isEqualTo(StockType.IN);
		assertThat(result).allSatisfy(history -> assertThat(history.productId()).isEqualTo(1L));
	}

}
