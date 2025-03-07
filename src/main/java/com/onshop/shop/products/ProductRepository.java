package com.onshop.shop.products;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.onshop.shop.seller.SellerProductsDTO;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId); // 기존 코드
    List<Product> findByCategoryIdIn(List<Long> categoryIds);
    
    @Query("SELECT p FROM Product p JOIN p.category c WHERE p.name LIKE %:query% OR c.name LIKE %:query%")
    public List<Product> searchByNameOrCategory(@Param("query") String query);
    
    
    // 판매자(점주)의 상품 조회
    @Query(value = "SELECT p.product_id AS productId, p.name AS name, p.price AS price, c.category_name AS categoryName, i.stock AS stock " +
            "FROM product p " +
            "JOIN category c ON c.category_id = p.category_id " + 
            "JOIN inventory i ON i.product_id = p.product_id " + 
            "WHERE p.seller_id = :sellerId", 
    nativeQuery = true)
    Page<SellerProductsDTO> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);
}
