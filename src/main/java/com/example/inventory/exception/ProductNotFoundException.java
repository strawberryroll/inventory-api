package com.example.inventory.exception;

// 없는 상품을 조회·수정·삭제하려 할 때 던진다.
public class ProductNotFoundException extends BusinessException {

	public ProductNotFoundException(Long id) {
		super(ErrorCode.PRODUCT_NOT_FOUND, "상품을 찾을 수 없습니다. id=" + id);
	}

}