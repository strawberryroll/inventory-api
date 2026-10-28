package com.example.inventory.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

// 요청 JSON의 키 이름과 필드 이름이 같으면 Spring이 자동으로 값을 채워 준다.
// 검증 규칙은 Controller의 @Valid에서 실행되고, 어기면 400 응답이 된다.
// @Size 값은 DB 컬럼 길이(productCode 50, name 100)와 맞춘다. (넘으면 DB 오류가 나기 때문)
// price·stockQuantity를 int가 아닌 Integer로 받는 이유: int는 값을 안 보내면 0이 되어 "누락"을 잡을 수 없다.
// Integer는 누락 시 null이 되어 @NotNull이 잡아 준다.
public record ProductCreateRequest(
		@NotBlank(message = "상품코드는 필수입니다")
		@Size(max = 50, message = "상품코드는 50자 이하여야 합니다")
		String productCode,

		@NotBlank(message = "상품명은 필수입니다")
		@Size(max = 100, message = "상품명은 100자 이하여야 합니다")
		String name,

		@NotNull(message = "가격은 필수입니다")
		@PositiveOrZero(message = "가격은 0 이상이어야 합니다")
		Integer price,

		@NotNull(message = "재고수량은 필수입니다")
		@PositiveOrZero(message = "재고수량은 0 이상이어야 합니다")
		Integer stockQuantity
) {
}
