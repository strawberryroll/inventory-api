package com.example.inventory.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

// 에러의 종류를 한곳에서 관리하는 enum. 항목마다 HTTP 상태와 기본 메시지를 가진다.
// 응답 JSON의 code 값은 항목 이름(name())을 그대로 쓴다. (예: PRODUCT_NOT_FOUND)
// 새 에러가 생기면 여기에 항목만 추가하면 되고, GlobalExceptionHandler는 고치지 않아도 된다.
@Getter
public enum ErrorCode {

	PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다"),
	DUPLICATE_PRODUCT_CODE(HttpStatus.CONFLICT, "이미 존재하는 상품코드입니다"),
	INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다"),
	PRODUCT_HAS_HISTORY(HttpStatus.CONFLICT, "입출고 이력이 있는 상품은 삭제할 수 없습니다"),

	// 아래 3개는 BusinessException이 아니라 Spring이 던지는 예외에 대응한다.
	INVALID_INPUT(HttpStatus.BAD_REQUEST, "입력값이 올바르지 않습니다"),
	INVALID_REQUEST_BODY(HttpStatus.BAD_REQUEST, "요청 본문을 읽을 수 없습니다. JSON 형식과 값의 타입을 확인하세요"),
	DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, "데이터 제약 조건에 위반되어 처리할 수 없습니다");

	private final HttpStatus status;
	private final String message;

	ErrorCode(HttpStatus status, String message) {
		this.status = status;
		this.message = message;
	}

}
