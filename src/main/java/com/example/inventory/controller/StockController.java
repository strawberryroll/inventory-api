package com.example.inventory.controller;

import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.StockHistoryResponse;
import com.example.inventory.dto.StockRequest;
import com.example.inventory.exception.ErrorResponse;
import com.example.inventory.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "입고·출고", description = "상품의 재고를 늘리고 줄이며, 상품별 입출고 이력을 조회")
@RestController
// 이 클래스의 URL은 모두 /api/products/{id} 로 시작한다
@RequestMapping("/api/products/{id}")
@RequiredArgsConstructor
public class StockController {

	private final StockService stockService;

	// POST /api/products/1/stock-in
	// @Valid: StockRequest의 검증 규칙(@Positive)을 실행한다. 어기면 Service에 오기 전에 400 응답
	@Operation(summary = "입고", description = "재고를 quantity만큼 늘리고 입고(IN) 이력을 남깁니다. 처리 후 상품을 돌려줍니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "입고 성공 (갱신된 상품)",
					content = @Content(schema = @Schema(implementation = ProductResponse.class))),
			@ApiResponse(responseCode = "400", description = "수량 오류 (INVALID_INPUT, INVALID_REQUEST_BODY)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "없는 상품 (PRODUCT_NOT_FOUND)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/stock-in")
	public ProductResponse stockIn(
			@Parameter(description = "상품 id", example = "1") @PathVariable Long id,
			@Valid @RequestBody StockRequest request) {
		return stockService.stockIn(id, request.quantity());
	}

	// POST /api/products/1/stock-out  (같은 StockRequest를 재사용한다)
	@Operation(summary = "출고", description = "재고를 quantity만큼 줄이고 출고(OUT) 이력을 남깁니다. 재고보다 많이 출고할 수 없으며, 실패하면 재고와 이력은 바뀌지 않습니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "출고 성공 (갱신된 상품)",
					content = @Content(schema = @Schema(implementation = ProductResponse.class))),
			@ApiResponse(responseCode = "400", description = "수량 오류 또는 재고 부족 (INVALID_INPUT, INVALID_REQUEST_BODY, INSUFFICIENT_STOCK)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
			@ApiResponse(responseCode = "404", description = "없는 상품 (PRODUCT_NOT_FOUND)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@PostMapping("/stock-out")
	public ProductResponse stockOut(
			@Parameter(description = "상품 id", example = "1") @PathVariable Long id,
			@Valid @RequestBody StockRequest request) {
		return stockService.stockOut(id, request.quantity());
	}

	// GET /api/products/1/stock-histories  -> 해당 상품의 입출고 이력 (최신순)
	@Operation(summary = "입출고 이력 조회", description = "상품의 입출고 이력을 최신순으로 돌려줍니다. 이력이 없으면 빈 배열입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "조회 성공 (이력이 없으면 빈 배열)",
					content = @Content(array = @ArraySchema(schema = @Schema(implementation = StockHistoryResponse.class)))),
			@ApiResponse(responseCode = "404", description = "없는 상품 (PRODUCT_NOT_FOUND)",
					content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
	})
	@GetMapping("/stock-histories")
	public List<StockHistoryResponse> getStockHistories(
			@Parameter(description = "상품 id", example = "1") @PathVariable Long id) {
		return stockService.getStockHistories(id);
	}

}
