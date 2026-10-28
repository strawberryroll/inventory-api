package com.example.inventory.controller;

import com.example.inventory.dto.PageResponse;
import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// 이 클래스의 모든 URL 앞에 /api/products가 붙는다
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	// POST /api/products
	// @RequestBody: 요청 본문의 JSON을 ProductCreateRequest 객체로 변환해 준다
	// @Valid: DTO의 검증 규칙(@NotBlank 등)을 먼저 실행한다. 어기면 이 메서드는 실행되지 않고 400 응답
	@PostMapping
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
		ProductResponse response = productService.create(request);
		// 201 Created: "새 리소스가 만들어졌다"는 의미의 상태 코드
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// GET /api/products/1  -> @PathVariable이 URL의 {id} 자리 값(1)을 id 파라미터로 넣어 준다
	@GetMapping("/{id}")
	public ProductResponse getProduct(@PathVariable Long id) {
		return productService.getProduct(id);
	}

	// GET /api/products?page=0&size=10  -> 페이지 단위 목록
	// Pageable을 파라미터로 선언하면 Spring이 쿼리스트링(page, size)을 읽어 객체로 만들어 준다. (@RequestParam 불필요)
	@GetMapping
	public PageResponse<ProductResponse> getProducts(Pageable pageable) {
		return productService.getProducts(pageable);
	}

	// PUT /api/products/1  -> 이름·가격 수정. 수정된 상품을 200 OK로 돌려준다
	@PutMapping("/{id}")
	public ProductResponse update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request) {
		return productService.update(id, request);
	}

	// DELETE /api/products/1  -> 삭제 후 204 No Content (돌려줄 본문이 없다는 의미)
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
