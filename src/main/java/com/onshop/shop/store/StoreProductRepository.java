
package com.onshop.shop.store;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onshop.shop.product.Product;

import java.util.List;

public interface StoreProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySellerId(Long sellerId);  // ✅ sellerId 기반 검색
}

