package com.example.inventory.service;

import com.example.inventory.domain.Product;
import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.exception.DuplicateProductCodeException;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// @Service: 비즈니스 규칙(중복 검사 등)을 담당하는 클래스로 Spring이 관리한다.
@Service
// final 필드를 받는 생성자를 자동 생성 -> Spring이 ProductRepository를 넣어 준다(의존성 주입)
@RequiredArgsConstructor
public class ProductService {

	private final ProductRepository productRepository;

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
		Product saved = productRepository.save(product);

		// 4. Entity -> 응답 DTO 변환 후 반환
		return ProductResponse.from(saved);
	}

	// readOnly = true: 조회 전용 트랜잭션. 데이터를 바꾸지 않는다는 의도를 밝히고 불필요한 비용을 줄인다.
	@Transactional(readOnly = true)
	public ProductResponse getProduct(Long id) {
		// findById는 Optional(값이 있을 수도 없을 수도 있는 상자)을 반환한다.
		// 비어 있으면 orElseThrow가 예외를 던져 404로 이어진다.
		Product product = productRepository.findById(id)
				.orElseThrow(() -> new ProductNotFoundException(id));
		return ProductResponse.from(product);
	}

	@Transactional(readOnly = true)
	public List<ProductResponse> getProducts() {
		// findAll() 결과의 각 Product를 ProductResponse로 바꿔 리스트로 모은다. (JS의 array.map과 비슷)
		return productRepository.findAll().stream()
				.map(ProductResponse::from)
				.toList();
	}

}
