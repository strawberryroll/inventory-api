package com.example.inventory.integration;

import com.example.inventory.domain.Product;
import com.example.inventory.domain.StockHistory;
import com.example.inventory.domain.StockType;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockHistoryRepository;
import com.example.inventory.service.StockService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;

// Mockito 단위 테스트가 증명하지 못하는 것: 진짜 DB와 @Transactional이 함께 동작할 때의 결과.
// H2 인메모리 DB(application-test.yaml)에서 Service를 실제 트랜잭션으로 실행하고 DB 상태를 직접 확인한다.
// 주의: 테스트 메서드에 @Transactional을 붙이지 않는다. 붙이면 테스트 전체가 하나의 트랜잭션이 되어
// Service의 커밋·롤백 동작을 확인할 수 없다.
@SpringBootTest
@ActiveProfiles("test")
class StockIntegrationTest {

	@Autowired
	private StockService stockService;

	@Autowired
	private ProductRepository productRepository;

	// 진짜 Repository를 감시(spy)하다가, 특정 테스트에서만 일부러 예외를 던지게 만들 수 있다.
	@MockitoSpyBean
	private StockHistoryRepository stockHistoryRepository;

	// 테스트마다 깨끗한 DB로 시작한다. 이력이 상품을 참조하므로 이력을 먼저 지운다.
	@BeforeEach
	void cleanUp() {
		stockHistoryRepository.deleteAll();
		productRepository.deleteAll();
	}

	private Product saveProduct(int stock) {
		return productRepository.save(new Product("IT-001", "통합 테스트 볼트", 500, stock));
	}

	@Test
	@DisplayName("입고: 재고 증가와 IN 이력이 DB에 함께 저장된다")
	void stockIn_savesStockAndHistory() {
		Product product = saveProduct(10);

		ProductResponse response = stockService.stockIn(product.getId(), 30);

		assertThat(response.stockQuantity()).isEqualTo(40);
		assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(40);
		List<StockHistory> histories = stockHistoryRepository.findByProductIdOrderByCreatedAtDesc(product.getId());
		assertThat(histories).hasSize(1);
		assertThat(histories.get(0).getType()).isEqualTo(StockType.IN);
		assertThat(histories.get(0).getQuantity()).isEqualTo(30);
	}

	@Test
	@DisplayName("출고 재고 부족: 예외가 나고 DB의 재고는 그대로, 이력은 0건이다")
	void stockOut_insufficientStock_changesNothing() {
		Product product = saveProduct(10);

		assertThatThrownBy(() -> stockService.stockOut(product.getId(), 20))
				.isInstanceOf(InsufficientStockException.class);

		assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(10);
		assertThat(stockHistoryRepository.count()).isZero();
	}

	// 이 테스트가 확인하는 것: 이력 저장이 실패해도 "재고만 줄어든" 상태가 DB에 남지 않는다. (최종 상태의 일관성)
	// 한계: JPA는 재고 UPDATE를 트랜잭션이 끝날 때 실행하므로, 이력 저장이 실패한 시점에는 아직 UPDATE가 나가기 전이다.
	// 즉 "이미 실행된 UPDATE를 되돌리는 롤백"까지 증명하는 테스트는 아니다.
	// @Transactional이 필요한 이유(재고 변경이 DB에 반영되려면 트랜잭션이 필요함)는 위의 입고 성공 테스트가 보여 준다.
	@Test
	@DisplayName("이력 저장이 실패하면 재고도 바뀌지 않는다 (재고와 이력이 함께 반영되거나 함께 반영되지 않음)")
	void stockOut_historySaveFails_stockStaysUnchanged() {
		Product product = saveProduct(10);
		// 이력을 저장하는 순간 실패하는 상황을 만든다.
		doThrow(new IllegalStateException("이력 저장 실패")).when(stockHistoryRepository).save(any(StockHistory.class));

		assertThatThrownBy(() -> stockService.stockOut(product.getId(), 4))
				.isInstanceOf(IllegalStateException.class);

		assertThat(productRepository.findById(product.getId()).orElseThrow().getStockQuantity()).isEqualTo(10);
		assertThat(stockHistoryRepository.count()).isZero();
	}

}
