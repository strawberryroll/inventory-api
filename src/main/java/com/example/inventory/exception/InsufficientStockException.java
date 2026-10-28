package com.example.inventory.exception;

// 재고보다 많이 출고하려 할 때 던진다.
// 트랜잭션 안에서 던져지면 재고 변경과 이력 저장이 모두 롤백된다.
public class InsufficientStockException extends BusinessException {

	public InsufficientStockException(int currentStock, int requestedQuantity) {
		super(ErrorCode.INSUFFICIENT_STOCK,
				"재고가 부족합니다. 현재 재고=" + currentStock + ", 요청 수량=" + requestedQuantity);
	}

}