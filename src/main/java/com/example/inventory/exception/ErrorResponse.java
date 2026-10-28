package com.example.inventory.exception;

// 모든 에러 응답의 공통 형식.
// code: 프론트엔드가 분기할 때 쓰는 안정적인 값 (message는 문장이라 바뀔 수 있다)
// message: 사람이 읽는 설명
public record ErrorResponse(
		int status,
		String code,
		String message
) {
}
