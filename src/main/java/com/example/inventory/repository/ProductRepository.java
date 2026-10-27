package com.example.inventory.repository;

import com.example.inventory.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// JpaRepository<엔티티, 기본키 타입>을 상속하면 save, findById, findAll, deleteById 등
// 기본 CRUD 메서드가 구현 코드 없이 자동으로 제공된다.
public interface ProductRepository extends JpaRepository<Product, Long> {

	// 메서드 이름만으로 쿼리가 만들어진다: SELECT ... FROM products WHERE product_code = ?
	Optional<Product> findByProductCode(String productCode);

	// SELECT 존재 여부만 확인. 상품 등록 시 코드 중복 검사에 사용할 예정
	boolean existsByProductCode(String productCode);

}
