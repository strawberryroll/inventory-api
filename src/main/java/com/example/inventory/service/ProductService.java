package com.example.inventory.service;

import com.example.inventory.domain.Product;
import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.exception.DuplicateProductCodeException;
import com.example.inventory.exception.ProductHasHistoryException;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service: 비즈니스 규칙(중복 검사 등)을 담당하는 클래스로 Spring이 관리한다.
@Service
// final 필드를 받는 생성자를 자동 생성 -> Spring이 ProductRepository를 넣어 준다(의존성 주입)
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;
	private final StockHistoryRepository stockHistoryRepository;

	// @Transactional: 메서드 전체를 하나의 트랜잭션으로 묶는다. 중간에 예외가 나면 DB 변경이 롤백된다.
	@Transactional
	public ProductResponse create(ProductCreateRequest request) {
		// 1. 상품코드 중복 검사 (규칙은 Service에서 판단)
		if (productRepository.existsByProductCode(request.productCode())) {
			throw new DuplicateProductCodeException(request.productCode());
		}

		// 2. DTO -> Entity 변환
		Product product = new Product(
				request.productCode(),
				request.name(),
				request.price(),
				request.stockQuantity()
		);

		// 3. 저장 (INSERT). 저장된 Entity에는 id와 createdAt이 채워져 돌아온다.
		Product saved;
		try {
			saved = productRepository.save(product);
		} catch (DataIntegrityViolationException e) {
			// 1번 검사를 통과한 뒤 같은 코드의 요청이 동시에 먼저 저장되면 DB의 unique 제약이 막아 준다.
			// 사전 검사는 친절한 메시지용이고, DB 제약은 그 틈을 막는 마지막 방어선이다.
			throw new DuplicateProductCodeException(request.productCode());
		}

		// 4. Entity -> 응답 DTO 변환 후 반환
		return ProductResponse.from(saved);
	}

	// readOnly = true: 조회 전용 트랜잭션. 데이터를 바꾸지 않는다는 의도를 밝히고 불필요한 비용을 줄인다.
	@Transactional(readOnly = true)
	public ProductResponse getProduct(Long id) {
		Product product = findProductOrThrow(id);
		return ProductResponse.from(product);
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> getProducts() {
		// findAll() 결과의 각 Product를 ProductResponse로 바꿔 리스트로 모은다. (JS의 array.map과 비슷)
		return productRepository.findAll().stream()
				.map(ProductResponse::from)
				.toList();
	}

	// 쓰기 작업이므로 readOnly를 쓰지 않는다. 변경 감지(UPDATE)는 트랜잭션이 끝날 때 실행되기 때문에
	// @Transactional이 없으면 값을 바꿔도 DB에 반영되지 않는다.
	@Transactional
	public ProductResponse update(Long id, ProductUpdateRequest request) {
		Product product = findProductOrThrow(id);

		// save()를 호출하지 않아도 된다. 조회한 엔티티의 값이 바뀌면 JPA가 달라진 부분을 찾아 UPDATE한다.
		product.update(request.name(), request.price());

		return ProductResponse.from(product);
	}

	@Transactional
	public void delete(Long id) {
		Product product = findProductOrThrow(id);

		// 이력이 있으면 삭제하지 않는다. (그냥 지우면 stock_histories의 외래키 제약 때문에 DB 오류 500이 난다)
		if (stockHistoryRepository.existsByProductId(id)) {
			throw new ProductHasHistoryException(id);
		}

		productRepository.delete(product);
	}

	// 단건 조회·수정·삭제가 함께 쓰는 "없으면 404 예외" 로직
	// findById는 Optional(값이 있을 수도 없을 수도 있는 상자)을 반환하고,
	// 비어 있으면 orElseThrow가 예외를 던진다.
	private Product findProductOrThrow(Long id) {
		return productRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException(id));
	}

}