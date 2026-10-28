package com.example.inventory.exception;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.List;

// @RestControllerAdvice: 모든 Controller에서 던져진 예외를 이 클래스가 한 곳에서 받아 JSON 응답으로 바꾼다.
// (Controller마다 try-catch를 쓰지 않아도 되고, 에러 응답 형식을 여기서 통일한다)
@RestControllerAdvice
public class GlobalExceptionHandler {

	// BusinessException의 자식 예외(상품 없음, 중복, 재고 부족, 이력 존재)를 모두 이 메서드 하나가 받는다.
	// 상태 코드와 code는 예외가 가진 ErrorCode에서 꺼내므로, 새 예외가 늘어도 이 메서드는 고치지 않는다.
	@ExceptionHandler(BusinessException.class)
	public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e) {
		return toResponse(e.getErrorCode(), e.getMessage());
	}

	// @Valid 검증에 실패하면 Spring이 이 예외를 던진다. Controller 메서드가 실행되기 전이라 Service·DB에는 닿지 않는다.
	// BindingResult에서 어느 필드가 어떤 규칙을 어겼는지 꺼내 errors 목록으로 내려준다.
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		List<ErrorResponse.FieldError> errors = e.getBindingResult().getFieldErrors().stream()
				.map(fieldError -> new ErrorResponse.FieldError(fieldError.getField(), fieldError.getDefaultMessage()))
				.toList();

		ErrorCode errorCode = ErrorCode.INVALID_INPUT;
		return ResponseEntity.status(errorCode.getStatus())
				.body(new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), errorCode.getMessage(), errors));
	}

	// 쿼리·경로 파라미터의 타입 변환이 실패했을 때. (예: threshold=abc, GET /api/products/abc)
	// 값 자체는 응답에 되돌려 주지 않고 어느 파라미터가 문제인지만 알려 준다.
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
		return toResponse(ErrorCode.INVALID_INPUT, "요청 파라미터 '" + e.getName() + "'의 형식이 올바르지 않습니다");
	}

	// 요청 JSON을 객체로 바꾸지 못할 때(문법 오류, 숫자 자리에 문자, 본문 없음). 검증 이전 단계의 오류다.
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadableBody(HttpMessageNotReadableException e) {
		ErrorCode errorCode = ErrorCode.INVALID_REQUEST_BODY;
		return toResponse(errorCode, errorCode.getMessage());
	}

	// 안전망: 사전 검사로 못 막은 DB 제약 위반(unique, 외래키 등)이 500 대신 409로 나가게 한다.
	// Spring이 DB 종류와 상관없이 제약 위반을 이 예외로 변환해 준다. SQL 오류 내용은 응답에 노출하지 않는다.
	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException e) {
		ErrorCode errorCode = ErrorCode.DATA_INTEGRITY_VIOLATION;
		return toResponse(errorCode, errorCode.getMessage());
	}

	// 응답 생성 코드를 한 곳으로 모은다. status는 ErrorCode에서, code는 ErrorCode의 이름에서 가져온다.
	private ResponseEntity<ErrorResponse> toResponse(ErrorCode errorCode, String message) {
		return ResponseEntity.status(errorCode.getStatus())
				.body(new ErrorResponse(errorCode.getStatus().value(), errorCode.name(), message));
	}

}