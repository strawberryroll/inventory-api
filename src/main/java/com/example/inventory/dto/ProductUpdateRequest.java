package com.example.inventory.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// 수정 요청 전용 DTO. 이름과 가격만 바꿀 수 있고, 상품코드·재고수량은 받지 않는다.
// (재고수량은 입고·출고로만 변경)
@Schema(description = "상품 수정 요청 (이름과 가격만 수정 가능)")
public record ProductUpdateRequest(
		@Schema(description = "상품명", example = "볼트 M10")
		@NotBlank(message = "상품명은 필수입니다")
		@Size(max = 100, message = "상품명은 100자 이하여야 합니다")
		String name,

		@Schema(description = "가격 (0 이상)", example = "700")
		@NotNull(message = "가격은 필수입니다")
		@PositiveOrZero(message = "가격은 0 이상이어야 합니다")
		Integer price
) {
}
