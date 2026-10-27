package com.example.inventory.controller;

import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
// 이 클래스의 모든 URL 앞에 /api/products가 붙는다
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	// POST /api/products
	// @RequestBody: 요청 본문의 JSON을 ProductCreateRequest 객체로 변환해 준다
	@PostMapping
	public ResponseEntity<ProductResponse> create(@RequestBody ProductCreateRequest request) {
		ProductResponse response = productService.create(request);
		// 201 Created: "새 리소스가 만들어졌다"는 의미의 상태 코드
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// GET /api/products/1  -> @PathVariable이 URL의 {id} 자리 값(1)을 id 파라미터로 넣어 준다
	@GetMapping("/{id}")
	public ProductResponse getProduct(@PathVariable Long id) {
		return productService.getProduct(id);
	}

	// GET /api/products  -> 전체 목록 (페이징·정렬은 8단계에서 추가 예정)
	@GetMapping
	public List<ProductResponse> getProducts() {
		return productService.getProducts();
	}

	// PUT /api/products/1  -> 이름·가격 수정. 수정된 상품을 200 OK로 돌려준다
	@PutMapping("/{id}")
	public ProductResponse update(@PathVariable Long id, @RequestBody ProductUpdateRequest request) {
		return productService.update(id, request);
	}

	// DELETE /api/products/1  -> 삭제 후 204 No Content (돌려줄 본문이 없다는 의미)
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
