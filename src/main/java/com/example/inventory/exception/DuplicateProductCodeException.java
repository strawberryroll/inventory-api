package com.example.inventory.exception;

// 이미 있는 상품코드로 등록하려 할 때 던진다.
public class DuplicateProductCodeException extends BusinessException {

	public DuplicateProductCodeException(String productCode) {
		super(ErrorCode.DUPLICATE_PRODUCT_CODE, "이미 존재하는 상품코드입니다: " + productCode);
	}

}