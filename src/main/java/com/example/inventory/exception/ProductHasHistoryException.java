package com.example.inventory.exception;

// 입출고 이력이 있는 상품을 삭제하려 할 때 던진다. 409 응답으로의 변환은 GlobalExceptionHandler가 담당한다.
// 이력은 재고 변동의 근거라서 상품과 함께 사라지면 안 된다.
public class ProductHasHistoryException extends RuntimeException {

	public ProductHasHistoryException(Long id) {
		super("입출고 이력이 있는 상품은 삭제할 수 없습니다. id=" + id);
	}

}
