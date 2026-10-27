package com.example.inventory.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

// @Entity: 이 클래스를 DB 테이블과 매핑한다. (ddl-auto: update 이므로 실행 시 products 테이블이 자동 생성됨)
@Entity
@Table(name = "products")
@Getter
// JPA는 기본 생성자가 필요하다. 아무데서나 빈 객체를 만들지 못하도록 protected로 제한
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

	// 기본키. IDENTITY: MySQL의 AUTO_INCREMENT에 번호 생성을 맡긴다.
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	// unique = true: DB 차원에서 상품 코드 중복을 막는다. (코드 중복 검사는 서비스에서도 따로 한다)
	@Column(nullable = false, unique = true, length = 50)
	private String productCode;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(nullable = false)
	private int price;

	@Column(nullable = false)
	private int stockQuantity;

	// 최초 저장 시 한 번만 세팅되고 이후 수정되지 않는다.
	@Column(nullable = false, updatable = false)
	private LocalDateTime createdAt;

	public Product(String productCode, String name, int price, int stockQuantity) {
		this.productCode = productCode;
		this.name = name;
		this.price = price;
		this.stockQuantity = stockQuantity;
	}

	// INSERT 직전에 JPA가 자동 호출한다. 그래서 createdAt을 직접 넣지 않아도 된다.
	@PrePersist
	void onCreate() {
		this.createdAt = LocalDateTime.now();
	}

}