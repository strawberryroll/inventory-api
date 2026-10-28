package com.example.inventory.service;

import com.example.inventory.domain.Product;
import com.example.inventory.domain.StockHistory;
import com.example.inventory.domain.StockType;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// 입고·출고처럼 재고 수량을 바꾸는 규칙을 담당한다.
@Service
@RequiredArgsConstructor
public class StockService {

	private final ProductRepository productRepository;
	private final StockHistoryRepository stockHistoryRepository;

	// 재고 변경(products)과 이력 저장(stock_histories)을 하나의 트랜잭션으로 묶는다.
	// 둘 중 하나라도 실패하면 모두 롤백되어, "재고는 늘었는데 이력은 없는" 상태가 생기지 않는다.
	@Transactional
	public ProductResponse stockIn(Long productId, int quantity) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));

		// 재고 증가. save()를 호출하지 않아도 트랜잭션이 끝날 때 변경 감지로 UPDATE가 실행된다.
		product.increaseStock(quantity);

		// 입고 이력 저장 (INSERT)
		stockHistoryRepository.save(new StockHistory(product, StockType.IN, quantity));

		return ProductResponse.from(product);
	}

}
