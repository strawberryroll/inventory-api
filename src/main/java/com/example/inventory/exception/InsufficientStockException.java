package com.example.inventory.exception;

// 재고보다 많이 출고하려 할 때 던진다. 400 응답으로의 변환은 GlobalExceptionHandler가 담당한다.
// RuntimeException이므로 @Transactional 메서드에서 던져지면 그 트랜잭션의 변경이 모두 롤백된다.
public class InsufficientStockException extends RuntimeException {

	public InsufficientStockException(int currentStock, int requestedQuantity) {
		super("재고가 부족합니다. 현재 재고=" + currentStock + ", 요청 수량=" + requestedQuantity);
	}

}
