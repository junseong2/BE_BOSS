
package com.onshop.shop.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onshop.shop.products.Product;

public interface StoreProductRepository extends JpaRepository<Product, Long> {
    List<Product> findBySellerId(Long sellerId);  // ✅ sellerId 기반 검색
}

