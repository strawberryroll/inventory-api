package com.example.inventory.dto;

import com.example.inventory.domain.StockHistory;
import com.example.inventory.domain.StockType;

import java.time.LocalDateTime;

// 입출고 이력 응답 DTO. 상품 정보 전체 대신 productId만 담는다.
public record StockHistoryResponse(
		Long id,
		Long productId,
		StockType type,
		int quantity,
		LocalDateTime createdAt
) {

	public static StockHistoryResponse from(StockHistory history) {
		return new StockHistoryResponse(
				history.getId(),
				// product는 LAZY라서 id만 꺼내는 것으로는 상품을 따로 조회하지 않는다.
				// (상품 이름 등을 꺼내면 이력마다 SELECT가 추가되는 N+1 문제가 생길 수 있다)
				history.getProduct().getId(),
				history.getType(),
				history.getQuantity(),
				history.getCreatedAt()
		);
	}

}