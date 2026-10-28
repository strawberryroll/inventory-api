package com.example.inventory.exception;

import lombok.Getter;

// 우리가 정의한 비즈니스 예외의 공통 부모. ErrorCode를 가지고 있어서
// GlobalExceptionHandler가 이 타입 하나만 받으면 자식 예외 전부를 처리할 수 있다.
// RuntimeException이므로 @Transactional 메서드에서 던져지면 그 트랜잭션은 롤백된다.
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	// 상세 메시지 없이 ErrorCode의 기본 메시지를 쓴다
	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	// 상세 메시지(어떤 id인지, 재고가 몇 개인지 등)를 덧붙인다
	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}

}