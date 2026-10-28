package com.example.inventory.repository;

import com.example.inventory.domain.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

	// 메서드 이름으로 쿼리가 만들어진다.
	// ProductId -> product.id 로 연관관계를 따라가며 WHERE product_id = ?, OrderByCreatedAtDesc -> 최신순 정렬
	List<StockHistory> findByProductIdOrderByCreatedAtDesc(Long productId);

}
