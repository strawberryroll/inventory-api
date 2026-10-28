package com.example.inventory.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

// 모든 에러 응답의 공통 형식.
// code: 프론트엔드가 분기할 때 쓰는 안정적인 값 (message는 문장이라 바뀔 수 있다)
// message: 사람이 읽는 설명
// errors: 입력 검증 오류일 때만 채우는 필드별 오류 목록. null이면 JSON에서 생략된다.
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(
		int status,
		String code,
		String message,
		List<FieldError> errors
) {

	// errors가 필요 없는 일반 오류용 생성자
	public ErrorResponse(int status, String code, String message) {
		this(status, code, message, null);
	}

	// 어느 필드가 왜 틀렸는지
	public record FieldError(String field, String message) {
	}

}
