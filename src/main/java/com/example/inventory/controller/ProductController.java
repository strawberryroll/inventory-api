package com.example.inventory.controller;

import com.example.inventory.dto.PageResponse;
import com.example.inventory.dto.ProductCreateRequest;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.ProductUpdateRequest;
import com.example.inventory.exception.ErrorResponse;
import com.example.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

// @Tag: Swagger UI에서 이 Controller의 API들을 하나의 그룹으로 묶어 보여 준다.
// @Operation / @ApiResponse / @Parameter 등은 문서에만 쓰이는 설명이고, API의 실제 동작에는 영향이 없다.
@Tag(name = "상품", description = "상품 등록·조회·수정·삭제, 검색, 재고 부족 조회")
@RestController
// 이 클래스의 모든 URL 앞에 /api/products가 붙는다
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

	private final ProductService productService;

	// POST /api/products
	// @RequestBody: 요청 본문의 JSON을 ProductCreateRequest 객체로 변환해 준다
	// @Valid: DTO의 검증 규칙(@NotBlank 등)을 먼저 실행한다. 어기면 이 메서드는 실행되지 않고 400 응답
	@Operation(summary = "상품 등록", description = "새 상품을 등록합니다. 상품코드는 중복될 수 없습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "201", description = "등록 성공",
					content = @Content(schema = @Schema(implementation = ProductResponse.class))),
			@ApiResponse(responseCode = "400", description = "입력값 오류 (INVALID_INPUT, INVALID_REQUEST_BODY)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "상품코드 중복 (DUPLICATE_PRODUCT_CODE)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping
	public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
		ProductResponse response = productService.create(request);
		// 201 Created: "새 리소스가 만들어졌다"는 의미의 상태 코드
		return ResponseEntity.status(HttpStatus.CREATED).body(response);
	}

	// GET /api/products/1  -> @PathVariable이 URL의 {id} 자리 값(1)을 id 파라미터로 넣어 준다
	@Operation(summary = "상품 단건 조회")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(schema = @Schema(implementation = ProductResponse.class))),
			@ApiResponse(responseCode = "400", description = "id 형식 오류 (INVALID_INPUT)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "없는 상품 (PRODUCT_NOT_FOUND)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/{id}")
	public ProductResponse getProduct(@Parameter(description = "상품 id", example = "1") @PathVariable Long id) {
		return productService.getProduct(id);
	}

	// GET /api/products?page=0&size=10            -> 페이지 단위 전체 목록
	// GET /api/products?keyword=볼트              -> 이름 또는 상품코드에 "볼트"가 포함된 상품 검색
	// Pageable을 파라미터로 선언하면 Spring이 쿼리스트링(page, size)을 읽어 객체로 만들어 준다. (@RequestParam 불필요)
	// @RequestParam(required = false): 쿼리스트링의 keyword를 받고, 안 보내도 오류가 나지 않는다. (null이 됨)
	// 문서화: Pageable을 그대로 두면 문서에 sort 파라미터까지 보이는데, 우리는 sort를 무시(최신순 고정)하므로
	// Pageable은 문서에서 숨기고 page, size만 직접 적어 준다.
	@Operation(summary = "상품 목록 조회·검색",
			description = "keyword가 있으면 상품명 또는 상품코드에 포함된 상품만 검색합니다(대소문자 무시). 정렬은 최신 등록순으로 고정됩니다.")
	@Parameters({
			@Parameter(name = "page", in = ParameterIn.QUERY, description = "페이지 번호 (0부터 시작)",
					schema = @Schema(type = "integer", defaultValue = "0")),
			@Parameter(name = "size", in = ParameterIn.QUERY, description = "한 페이지 크기 (기본 10, 최대 100)",
					schema = @Schema(type = "integer", defaultValue = "10"))
	})
	@GetMapping
	public PageResponse<ProductResponse> getProducts(
			@Parameter(description = "상품명 또는 상품코드에 포함된 검색어", example = "볼트")
			@RequestParam(required = false) String keyword,
			@Parameter(hidden = true) Pageable pageable) {
		return productService.getProducts(keyword, pageable);
	}

	// GET /api/products/low-stock?threshold=10  -> 재고가 threshold 이하인 상품 (재고 적은 순, 페이징)
	// /api/products/{id} 와 모양이 비슷하지만, Spring은 고정 경로(/low-stock)를 변수 경로({id})보다 먼저 매칭하므로 충돌하지 않는다.
	// defaultValue: threshold를 안 보내면 10으로 처리한다.
	@Operation(summary = "재고 부족 상품 조회",
			description = "재고가 threshold 이하인 상품을 재고가 적은 순서로 조회합니다.")
	@Parameters({
			@Parameter(name = "page", in = ParameterIn.QUERY, description = "페이지 번호 (0부터 시작)",
					schema = @Schema(type = "integer", defaultValue = "0")),
			@Parameter(name = "size", in = ParameterIn.QUERY, description = "한 페이지 크기 (기본 10, 최대 100)",
					schema = @Schema(type = "integer", defaultValue = "10"))
	})
	@ApiResponses({
			// PageResponse<ProductResponse>는 제네릭이라 클래스로 지정할 수 없어서, 목록 API가 이미 만든 스키마를 이름으로 가리킨다.
			@ApiResponse(responseCode = "200", description = "조회 성공",
					content = @Content(schema = @Schema(ref = "#/components/schemas/PageResponseProductResponse"))),
			@ApiResponse(responseCode = "400", description = "threshold가 음수이거나 숫자가 아님 (INVALID_INPUT)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/low-stock")
	public PageResponse<ProductResponse> getLowStockProducts(
			@Parameter(description = "이 값 이하의 재고를 가진 상품을 조회 (0 이상)", example = "10")
			@RequestParam(defaultValue = "10") int threshold,
			@Parameter(hidden = true) Pageable pageable) {
		return productService.getLowStockProducts(threshold, pageable);
	}

	// PUT /api/products/1  -> 이름·가격 수정. 수정된 상품을 200 OK로 돌려준다
	@Operation(summary = "상품 수정", description = "이름과 가격만 수정할 수 있습니다. 상품코드와 재고수량은 바뀌지 않습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "수정 성공",
					content = @Content(schema = @Schema(implementation = ProductResponse.class))),
			@ApiResponse(responseCode = "400", description = "입력값 오류 (INVALID_INPUT, INVALID_REQUEST_BODY)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "없는 상품 (PRODUCT_NOT_FOUND)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PutMapping("/{id}")
	public ProductResponse update(
			@Parameter(description = "상품 id", example = "1") @PathVariable Long id,
			@Valid @RequestBody ProductUpdateRequest request) {
		return productService.update(id, request);
	}

	// DELETE /api/products/1  -> 삭제 후 204 No Content (돌려줄 본문이 없다는 의미)
	@Operation(summary = "상품 삭제", description = "입출고 이력이 있는 상품은 삭제할 수 없습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "204", description = "삭제 성공"),
			@ApiResponse(responseCode = "404", description = "없는 상품 (PRODUCT_NOT_FOUND)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "409", description = "입출고 이력이 있어 삭제 불가 (PRODUCT_HAS_HISTORY)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@Parameter(description = "상품 id", example = "1") @PathVariable Long id) {
		productService.delete(id);
		return ResponseEntity.noContent().build();
	}

}
