package com.example.inventory.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// 재고보다 많이 출고하려 할 때 던진다. Controller 밖으로 나가면 Spring이 HTTP 400(Bad Request)으로 응답한다.
// RuntimeException이므로 @Transactional 메서드에서 던져지면 그 트랜잭션의 변경이 모두 롤백된다.
// (7단계에서 @RestControllerAdvice 전역 예외 처리로 교체 예정)
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InsufficientStockException extends RuntimeException {

	public InsufficientStockException(int currentStock, int requestedQuantity) {
		super("재고가 부족합니다. 현재 재고=" + currentStock + ", 요청 수량=" + requestedQuantity);
	}

}
