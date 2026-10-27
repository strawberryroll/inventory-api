package com.example.inventory.dto;

// record: getter, 생성자, equals/hashCode를 자동으로 만들어 주는 Java 21의 간단한 데이터 클래스.
// 요청 JSON의 키 이름과 필드 이름이 같으면 Spring이 자동으로 값을 채워 준다.
// (입력값 검증 @NotBlank 등은 7단계에서 추가 예정)
public record ProductCreateRequest(
		String productCode,
		String name,
		int price,
		int stockQuantity
) {
}
