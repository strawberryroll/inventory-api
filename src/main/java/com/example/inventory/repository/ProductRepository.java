package com.example.inventory.repository;

import com.example.inventory.domain.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<엔티티, 기본키 타입>을 상속하면 save, findById, findAll, deleteById 등
// 기본 CRUD 메서드가 구현 코드 없이 자동으로 제공된다.
public interface ProductRepository extends JpaRepository<Product, Long> {

	// 메서드 이름만으로 쿼리가 만들어진다: SELECT ... FROM products WHERE product_code = ?
	Optional<Product> findByProductCode(String productCode);

	// SELECT 존재 여부만 확인. 상품 등록 시 코드 중복 검사에 사용할 예정
	boolean existsByProductCode(String productCode);

	// 검색: 이름 또는 상품코드에 키워드가 포함된 상품 (대소문자 무시, 페이징)
	// Containing -> LIKE '%키워드%', IgnoreCase -> 대소문자 무시, Or -> 둘 중 하나만 맞아도 포함
	// 같은 키워드를 name, productCode 두 인자에 모두 넘긴다. (키워드의 %, _ 는 Spring Data가 이스케이프해 준다)
	Page<Product> findByNameContainingIgnoreCaseOrProductCodeContainingIgnoreCase(
			String name, String productCode, Pageable pageable);

	// 재고 부족 조회: 재고가 threshold "이하"인 상품 (LessThanEqual -> WHERE stock_quantity <= ?)
	// (LessThan은 <, GreaterThanEqual은 >= 로 만들어진다)
	Page<Product> findByStockQuantityLessThanEqual(int threshold, Pageable pageable);

}
