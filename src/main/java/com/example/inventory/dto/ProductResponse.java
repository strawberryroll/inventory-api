package com.example.inventory.dto;

import com.example.inventory.domain.Product;

import java.time.LocalDateTime;

// 응답 전용 객체. Entity를 그대로 반환하지 않고 필요한 값만 담아서 내보낸다.
public record ProductResponse(
		Long id,
		String productCode,
		String name,
		int price,
		int stockQuantity,
		LocalDateTime createdAt
) {

	// Entity -> DTO 변환. Service에서 ProductResponse.from(product)로 사용한다.
	public static ProductResponse from(Product product) {
		return new ProductResponse(
				product.getId(),
				product.getProductCode(),
				product.getName(),
				product.getPrice(),
				product.getStockQuantity(),
				product.getCreatedAt()
		);
	}

}