package com.example.inventory.service;

import com.example.inventory.domain.Product;
import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.exception.BusinessException;
import com.example.inventory.exception.DuplicateProductCodeException;
import com.example.inventory.exception.ErrorCode;
import com.example.inventory.exception.ProductHasHistoryException;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

// Repository를 가짜(mock)로 바꿔 DB 없이 ProductService의 규칙(중복, 이력 확인, 정렬 고정 등)만 검증한다.
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private StockHistoryRepository stockHistoryRepository;

	@InjectMocks
	private ProductService productService;

	// ------------------------------------------------------------ 등록

	@Test
	@DisplayName("등록 성공: 저장하고 요청한 값이 담긴 응답을 돌려준다")
	void create_success() {
		ProductCreateRequest request = new ProductCreateRequest("P-001", "볼트 M8", 500, 10);
		given(productRepository.existsByProductCode("P-001")).willReturn(false);
		// save()가 받은 객체를 그대로 돌려주게 한다. (진짜 DB라면 id와 createdAt이 채워져 돌아온다)
		given(productRepository.save(any(Product.class))).willAnswer(invocation -> invocation.getArgument(0));

		ProductResponse response = productService.create(request);

		assertThat(response.productCode()).isEqualTo("P-001");
		assertThat(response.name()).isEqualTo("볼트 M8");
		assertThat(response.price()).isEqualTo(500);
		assertThat(response.stockQuantity()).isEqualTo(10);
		verify(productRepository).save(any(Product.class));
	}

	@Test
	@DisplayName("등록: 상품코드가 중복이면 예외가 나고 저장하지 않는다")
	void create_duplicateCode_throws() {
		ProductCreateRequest request = new ProductCreateRequest("P-001", "볼트 M8", 500, 10);
		given(productRepository.existsByProductCode("P-001")).willReturn(true);

		assertThatThrownBy(() -> productService.create(request))
				.isInstanceOf(DuplicateProductCodeException.class)
				.hasMessageContaining("P-001");

		verify(productRepository, never()).save(any(Product.class));
	}

	@Test
	@DisplayName("등록: 사전 검사를 통과해도 저장 중 DB 제약 위반이 나면 중복 예외로 바꾼다 (동시 요청 방어)")
	void create_dbConstraintViolation_throwsDuplicate() {
		ProductCreateRequest request = new ProductCreateRequest("P-001", "볼트 M8", 500, 10);
		given(productRepository.existsByProductCode("P-001")).willReturn(false);
		given(productRepository.save(any(Product.class))).willThrow(new DataIntegrityViolationException("unique 위반"));

		assertThatThrownBy(() -> productService.create(request))
				.isInstanceOf(DuplicateProductCodeException.class);
	}

	// ------------------------------------------------------------ 수정

	@Test
	@DisplayName("수정 성공: 이름과 가격이 바뀌고 재고는 그대로다")
	void update_success() {
		Product product = new Product("P-001", "볼트 M8", 500, 10);
		given(productRepository.findById(1L)).willReturn(Optional.of(product));

		ProductResponse response = productService.update(1L, new ProductUpdateRequest("볼트 M10", 700));

		assertThat(response.name()).isEqualTo("볼트 M10");
		assertThat(response.price()).isEqualTo(700);
		assertThat(response.stockQuantity()).isEqualTo(10);
	}

	@Test
	@DisplayName("수정: 없는 상품이면 예외가 난다")
	void update_productNotFound_throws() {
		given(productRepository.findById(999L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> productService.update(999L, new ProductUpdateRequest("볼트 M10", 700)))
				.isInstanceOf(ProductNotFoundException.class);
	}

	// ------------------------------------------------------------ 삭제

	@Test
	@DisplayName("삭제: 이력이 있는 상품은 예외가 나고 삭제하지 않는다")
	void delete_hasHistory_throwsAndDoesNotDelete() {
		Product product = new Product("P-001", "볼트 M8", 500, 10);
		given(productRepository.findById(1L)).willReturn(Optional.of(product));
		given(stockHistoryRepository.existsByProductId(1L)).willReturn(true);

		assertThatThrownBy(() -> productService.delete(1L))
				.isInstanceOf(ProductHasHistoryException.class);

		verify(productRepository, never()).delete(any(Product.class));
	}

	@Test
	@DisplayName("삭제 성공: 이력이 없으면 삭제된다")
	void delete_noHistory_deletes() {
		Product product = new Product("P-001", "볼트 M8", 500, 10);
		given(productRepository.findById(1L)).willReturn(Optional.of(product));
		given(stockHistoryRepository.existsByProductId(1L)).willReturn(false);

		productService.delete(1L);

		verify(productRepository).delete(product);
	}

	@Test
	@DisplayName("삭제: 없는 상품이면 예외가 나고 이력 확인까지 가지 않는다")
	void delete_productNotFound_throws() {
		given(productRepository.findById(999L)).willReturn(Optional.empty());

		assertThatThrownBy(() -> productService.delete(999L))
				.isInstanceOf(ProductNotFoundException.class);

		verify(stockHistoryRepository, never()).existsByProductId(anyLong());
	}

	// ------------------------------------------------------------ 목록·검색

	// @ParameterizedTest: 같은 테스트를 여러 입력값으로 반복 실행한다. (null, 빈 문자열, 공백만)
	@ParameterizedTest
	@NullAndEmptySource
	@ValueSource(strings = {"   "})
	@DisplayName("목록: 키워드가 없거나 공백이면 검색하지 않고 전체를 조회한다")
	void getProducts_blankKeyword_returnsAll(String keyword) {
		given(productRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of()));

		productService.getProducts(keyword, PageRequest.of(0, 10));

		verify(productRepository).findAll(any(Pageable.class));
		verify(productRepository, never())
				.findByNameContainingIgnoreCaseOrProductCodeContainingIgnoreCase(any(), any(), any());
	}

	@Test
	@DisplayName("목록: 키워드의 앞뒤 공백을 제거하고 이름·코드 양쪽으로 검색한다")
	void getProducts_keyword_searchesWithTrimmedKeyword() {
		given(productRepository.findByNameContainingIgnoreCaseOrProductCodeContainingIgnoreCase(
				eq("볼트"), eq("볼트"), any(Pageable.class))).willReturn(new PageImpl<>(List.of()));

		productService.getProducts("  볼트  ", PageRequest.of(0, 10));

		verify(productRepository).findByNameContainingIgnoreCaseOrProductCodeContainingIgnoreCase(
				eq("볼트"), eq("볼트"), any(Pageable.class));
		verify(productRepository, never()).findAll(any(Pageable.class));
	}

	@Test
	@DisplayName("목록: 클라이언트가 보낸 정렬은 무시하고 id 내림차순으로 고정한다")
	void getProducts_ignoresClientSort() {
		given(productRepository.findAll(any(Pageable.class))).willReturn(new PageImpl<>(List.of()));

		// 클라이언트가 가격 정렬을 요청한 상황
		productService.getProducts(null, PageRequest.of(2, 5, Sort.by("price")));

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(productRepository).findAll(captor.capture());
		Pageable used = captor.getValue();
		assertThat(used.getPageNumber()).isEqualTo(2);
		assertThat(used.getPageSize()).isEqualTo(5);
		assertThat(used.getSort().stream().map(order -> order.getProperty() + " " + order.getDirection()).toList())
				.containsExactly("id DESC");
	}

	// ------------------------------------------------------------ 재고 부족 조회

	@Test
	@DisplayName("재고 부족 조회: threshold가 음수이면 INVALID_INPUT 예외이고 조회하지 않는다")
	void getLowStockProducts_negativeThreshold_throws() {
		assertThatThrownBy(() -> productService.getLowStockProducts(-1, PageRequest.of(0, 10)))
				.isInstanceOfSatisfying(BusinessException.class,
						e -> assertThat(e.getErrorCode()).isEqualTo(ErrorCode.INVALID_INPUT));

		verifyNoInteractions(productRepository);
	}

	@Test
	@DisplayName("재고 부족 조회: 재고 오름차순, 같으면 id 내림차순으로 정렬을 고정한다")
	void getLowStockProducts_fixesSort() {
		given(productRepository.findByStockQuantityLessThanEqual(eq(10), any(Pageable.class)))
				.willReturn(new PageImpl<>(List.of()));

		productService.getLowStockProducts(10, PageRequest.of(1, 4, Sort.by("id")));

		ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
		verify(productRepository).findByStockQuantityLessThanEqual(eq(10), captor.capture());
		Pageable used = captor.getValue();
		assertThat(used.getPageNumber()).isEqualTo(1);
		assertThat(used.getPageSize()).isEqualTo(4);
		assertThat(used.getSort().stream().map(order -> order.getProperty() + " " + order.getDirection()).toList())
				.containsExactly("stockQuantity ASC", "id DESC");
	}

}
