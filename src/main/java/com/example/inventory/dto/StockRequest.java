package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

// 입고·출고 요청 공용 DTO.
// @Positive: 1 이상만 허용. 0이나 음수는 400 Bad Request. 음수 수량으로 재고 검사를 우회하는 것을 막는다.
// int가 아닌 Integer로 받는 이유: int는 값을 안 보내면 변환 자체가 실패해 "어느 필드가 문제인지" 알려 줄 수 없다.
// Integer는 누락 시 null이 되어 @NotNull이 필드 단위 오류(INVALID_INPUT)로 알려 준다.
@Schema(description = "입고·출고 요청")
public record StockRequest(
		@Schema(description = "입고 또는 출고할 수량 (1 이상)", example = "10")
		@NotNull(message = "수량은 필수입니다")
		@Positive(message = "수량은 1 이상이어야 합니다")
		Integer quantity
) {
}
