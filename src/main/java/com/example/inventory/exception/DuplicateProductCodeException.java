package com.example.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 이 예외가 Controller 밖으로 던져지면 Spring이 HTTP 409(Conflict)로 응답한다.
// (7단계에서 @RestControllerAdvice 전역 예외 처리로 교체 예정)
@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateProductCodeException extends RuntimeException {

	public DuplicateProductCodeException(String productCode) {
		super("이미 존재하는 상품코드입니다: " + productCode);
	}

}
