package com.example.inventory.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

// @RestControllerAdvice: 모든 Controller에서 던져진 예외를 이 클래스가 한 곳에서 받아 JSON 응답으로 바꾼다.
// (Controller마다 try-catch를 쓰지 않아도 되고, 에러 응답 형식을 여기서 통일한다)
@RestControllerAdvice
public class GlobalExceptionHandler {

	// @ExceptionHandler: 이 예외가 던져지면 아래 메서드가 실행된다. 상태 코드와 응답 본문을 모두 정할 수 있다.
	@ExceptionHandler(ProductNotFoundException.class)
	public ResponseEntity<ErrorResponse> handleProductNotFound(ProductNotFoundException e) {
		return toResponse(HttpStatus.NOT_FOUND, "PRODUCT_NOT_FOUND", e.getMessage());
	}

	@ExceptionHandler(DuplicateProductCodeException.class)
	public ResponseEntity<ErrorResponse> handleDuplicateProductCode(DuplicateProductCodeException e) {
		return toResponse(HttpStatus.CONFLICT, "DUPLICATE_PRODUCT_CODE", e.getMessage());
	}

	@ExceptionHandler(InsufficientStockException.class)
	public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException e) {
		return toResponse(HttpStatus.BAD_REQUEST, "INSUFFICIENT_STOCK", e.getMessage());
	}

	@ExceptionHandler(ProductHasHistoryException.class)
	public ResponseEntity<ErrorResponse> handleProductHasHistory(ProductHasHistoryException e) {
		return toResponse(HttpStatus.CONFLICT, "PRODUCT_HAS_HISTORY", e.getMessage());
	}

	// 안전망: 사전 검사로 못 막은 DB 제약 위반(unique, 외래키 등)이 500 대신 409로 나가게 한다.
	// Spring이 DB 종류와 상관없이 제약 위반을 이 예외로 변환해 준다. SQL 오류 내용은 응답에 노출하지 않는다.
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
		return toResponse(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION", "데이터 제약 조건에 위반되어 처리할 수 없습니다");
	}

	// @Valid 검증에 실패하면 Spring이 이 예외를 던진다. Controller 메서드가 실행되기 전이라 Service·DB에는 닿지 않는다.
	// BindingResult에서 어느 필드가 어떤 규칙을 어겼는지 꺼내 errors 목록으로 내려준다.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		List<ErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> new ErrorResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
				.toList();

		HttpStatus status = HttpStatus.BAD_REQUEST;
		return ResponseEntity.status(status)
				.body(new ErrorResponse(status.value(), "INVALID_INPUT", "입력값이 올바르지 않습니다", errors));
	}

	// 요청 JSON을 객체로 바꾸지 못할 때(문법 오류, 숫자 자리에 문자, 본문 없음). 검증 이전 단계의 오류다.
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
		return toResponse(HttpStatus.BAD_REQUEST, "INVALID_REQUEST_BODY",
				"요청 본문을 읽을 수 없습니다. JSON 형식과 값의 타입을 확인하세요");
	}

	// 핸들러마다 반복되는 응답 생성 코드를 한 곳으로 모은다.
	private ResponseEntity<ErrorResponse> toResponse(HttpStatus status, String code, String message) {
		return ResponseEntity.status(status).body(new ErrorResponse(status.value(), code, message));
	}

}
