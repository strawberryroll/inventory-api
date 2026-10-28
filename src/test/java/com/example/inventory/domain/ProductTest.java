package com.example.inventory.domain;

import com.example.inventory.exception.InsufficientStockException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

// Spring도 DB도 없이 자바 객체만으로 하는 가장 단순한 단위 테스트.
// 재고 규칙("음수가 될 수 없다")의 핵심이 엔티티 메서드 안에 있으므로 경계값까지 꼼꼼히 확인한다.
class ProductTest {

	private Product product;

	// 각 테스트가 시작되기 전에 항상 재고 10인 상품을 새로 만든다. (테스트끼리 영향을 주지 않도록)
	@BeforeEach
	void setUp() {
		product = new Product("P-001", "볼트 M8", 500, 10);
	}

	@Test
	@DisplayName("출고: 재고보다 적게 출고하면 재고가 줄어든다")
	void decreaseStock_lessThanStock_decreases() {
		product.decreaseStock(3);

		assertThat(product.getStockQuantity()).isEqualTo(7);
	}

	@Test
	@DisplayName("출고: 재고와 같은 수량을 출고하면 0이 된다 (경계값)")
	void decreaseStock_sameAsStock_becomesZero() {
		product.decreaseStock(10);

		assertThat(product.getStockQuantity()).isZero();
	}

	@Test
	@DisplayName("출고: 재고보다 1개라도 많으면 예외가 나고 재고는 그대로다 (경계값)")
	void decreaseStock_overStock_throwsAndKeepsStock() {
		assertThatThrownBy(() -> product.decreaseStock(11))
				.isInstanceOf(InsufficientStockException.class)
				.hasMessageContaining("현재 재고=10")
				.hasMessageContaining("요청 수량=11");

		assertThat(product.getStockQuantity()).isEqualTo(10);
	}

	@Test
	@DisplayName("입고: 재고가 늘어난다")
	void increaseStock_increases() {
		product.increaseStock(30);

		assertThat(product.getStockQuantity()).isEqualTo(40);
	}

	@Test
	@DisplayName("수정: 이름과 가격만 바뀌고 상품코드와 재고는 그대로다")
	void update_changesOnlyNameAndPrice() {
		product.update("볼트 M10", 700);

		assertThat(product.getName()).isEqualTo("볼트 M10");
		assertThat(product.getPrice()).isEqualTo(700);
		assertThat(product.getProductCode()).isEqualTo("P-001");
		assertThat(product.getStockQuantity()).isEqualTo(10);
	}

}
