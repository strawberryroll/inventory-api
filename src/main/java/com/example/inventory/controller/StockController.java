package com.example.inventory.controller;

import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.StockRequest;
import com.example.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
// 이 클래스의 URL은 모두 /api/products/{id} 로 시작한다
@RequestMapping("/api/products/{id}")
@RequiredArgsConstructor
public class StockController {

	private final StockService stockService;

	// POST /api/products/1/stock-in
	// @Valid: StockRequest의 검증 규칙(@Positive)을 실행한다. 어기면 Service에 오기 전에 400 응답
	@PostMapping("/stock-in")
	public ProductResponse stockIn(@PathVariable Long id, @Valid @RequestBody StockRequest request) {
		return stockService.stockIn(id, request.quantity());
	}

}
