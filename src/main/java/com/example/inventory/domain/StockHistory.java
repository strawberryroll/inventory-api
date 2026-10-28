package com.example.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// 입출고 이력 한 건 = stock_histories 테이블의 한 행
@Entity
@Table(name = "stock_histories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StockHistory {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// @ManyToOne: 이력 여러 건(N)이 상품 하나(1)를 가리키는 관계. DB에는 product_id 외래키(FK) 컬럼이 생긴다.
	// LAZY: 이력만 조회할 때 상품까지 매번 가져오지 않고, product를 실제로 사용할 때 조회한다.
	// 상품 없는 이력은 있을 수 없으므로 optional = false, nullable = false
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	// STRING: "IN", "OUT" 글자로 저장한다. ORDINAL(0, 1)은 enum 순서가 바뀌면 기존 데이터 의미가 틀어진다.
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 10)
	private StockType type;

	@Column(nullable = false)
	private int quantity;

	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public StockHistory(Product product, StockType type, int quantity) {
		this.product = product;
		this.type = type;
		this.quantity = quantity;
	}

	// INSERT 직전에 JPA가 자동 호출한다. 그래서 createdAt을 직접 넣지 않아도 된다.
	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}
