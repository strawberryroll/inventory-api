package com.example.inventory.dto;

// 수정 요청 전용 DTO. 이름과 가격만 바꿀 수 있고, 상품코드·재고수량은 받지 않는다.
// (재고수량은 6단계의 입고·출고로만 변경)
public record ProductUpdateRequest(
		String name,
		int price
) {
}
