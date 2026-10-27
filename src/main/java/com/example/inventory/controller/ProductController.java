package com.example.inventory.controller;

import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
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
	@PostMapping
	public ResponseEntity<ProductResponse> create(@RequestBody ProductCreateRequest request) {
		ProductResponse response = productService.create(request);
		// 201 Created: "새 리소스가 만들어졌다"는 의미의 상태 코드
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

}
