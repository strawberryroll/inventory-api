package com.example.inventory.service;

import com.example.inventory.domain.Product;
import com.example.inventory.domain.StockHistory;
import com.example.inventory.domain.StockType;
import com.example.inventory.dto.ProductResponse;
import com.example.inventory.dto.StockHistoryResponse;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.repository.ProductRepository;
import com.example.inventory.repository.StockHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

	// 출고도 재고 변경과 이력 저장을 하나의 트랜잭션으로 묶는다.
	@Transactional
	public ProductResponse stockOut(Long productId, int quantity) {
		Product product = productRepository.findById(productId)
				.orElseThrow(() -> new ProductNotFoundException(productId));

		// 재고가 부족하면 여기서 InsufficientStockException이 던져진다.
		// 그러면 아래 이력 저장은 실행되지 않고, 트랜잭션도 롤백되어 DB는 아무것도 바뀌지 않는다.
		product.decreaseStock(quantity);

		// 출고 이력 저장 (INSERT)
		stockHistoryRepository.save(new StockHistory(product, StockType.OUT, quantity));

		return ProductResponse.from(product);
	}

	@Transactional(readOnly = true)
	public List<StockHistoryResponse> getStockHistories(Long productId) {
		// 없는 상품이면 404. 이 검사가 없으면 없는 id도 빈 목록(200 [])이 나와서
		// "이력이 없는 상품"과 "없는 상품"을 구분할 수 없다.
		if (!productRepository.existsById(productId)) {
			throw new ProductNotFoundException(productId);
		}

		// DTO 변환은 트랜잭션 안(Service)에서 한다. 지연 로딩 객체를 안전하게 읽기 위해서다.
		return stockHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId).stream()
				.map(StockHistoryResponse::from)
				.toList();
	}

}
