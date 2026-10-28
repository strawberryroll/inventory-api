package com.example.inventory.exception;

// 없는 상품을 조회·수정·삭제하려 할 때 던진다. 404 응답으로의 변환은 GlobalExceptionHandler가 담당한다.
public class ProductNotFoundException extends RuntimeException {

	public ProductNotFoundException(Long id) {
		super("상품을 찾을 수 없습니다. id=" + id);
	}

}
