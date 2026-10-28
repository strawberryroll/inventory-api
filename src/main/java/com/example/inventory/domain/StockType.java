package com.example.inventory.domain;

// 입출고 구분. 수량(quantity)은 항상 양수로 저장하고, 재고가 늘었는지 줄었는지는 이 값으로 구분한다.
public enum StockType {
	IN,  // 입고
	OUT  // 출고
}
