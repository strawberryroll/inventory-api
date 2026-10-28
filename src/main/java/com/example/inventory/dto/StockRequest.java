package com.example.inventory.dto;

import jakarta.validation.constraints.Positive;

// 입고·출고 요청 공용 DTO.
// @Positive: 1 이상만 허용. 0이나 음수, 값을 보내지 않은 경우(int 기본값 0)는 400 Bad Request
// 음수 수량으로 재고 검사를 우회하는 것을 막는다.
public record StockRequest(
		@Positive(message = "수량은 1 이상이어야 합니다")
		int quantity
) {
}
