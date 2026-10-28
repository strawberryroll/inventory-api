package com.example.inventory.repository;

import com.example.inventory.domain.StockHistory;
import org.springframework.data.jpa.repository.JpaRepository;

// 기본 CRUD(save, findById 등)만 사용한다. 상품별 이력 조회 메서드는 6-4에서 추가 예정
public interface StockHistoryRepository extends JpaRepository<StockHistory, Long> {

}
