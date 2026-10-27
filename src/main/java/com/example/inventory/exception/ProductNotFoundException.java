package com.example.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 이 예외가 Controller 밖으로 던져지면 Spring이 HTTP 404(Not Found)로 응답한다.
// (7단계에서 @RestControllerAdvice 전역 예외 처리로 교체 예정)
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException(Long id) {
		super("상품을 찾을 수 없습니다. id=" + id);
	}

}
