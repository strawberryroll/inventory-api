package com.example.inventory.exception;

// 이미 있는 상품코드로 등록하려 할 때 던진다. 409 응답으로의 변환은 GlobalExceptionHandler가 담당한다.
public class DuplicateProductCodeException extends RuntimeException {

	public DuplicateProductCodeException(String productCode) {
		super("이미 존재하는 상품코드입니다: " + productCode);
	}

}
