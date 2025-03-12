
package com.onshop.shop.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository("productRepository2")
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySeller_SellerId(Long sellerId);  // seller.sellerId를 참조하여 상품 조회
}
