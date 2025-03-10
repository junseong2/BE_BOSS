package com.onshop.shop.products;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.onshop.shop.seller.products.SellerProductsDTO;

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
    
    // 점주 전용 상품 조회(단일)
    @Query("SELECT p FROM Product p WHERE p.sellerId = :sellerId AND p.productId = :productId")
    Product findBySellerIdAndProductId(@Param("sellerId") Long sellerId, @Param("productId") Long productId);
    
    // 점주 전용 상품 검색
    @Query(value = "SELECT p.product_id AS productId, p.name AS name, p.price AS price, c.category_name AS categoryName, i.stock AS stock " +
            "FROM product p " +
            "JOIN category c ON c.category_id = p.category_id " + 
            "LEFT JOIN inventory i ON i.product_id = p.product_id " + 
            "WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :name, '%')", 
    nativeQuery = true)
    Page<SellerProductsDTO> findByNameAndSellerId(@Param("name") String name, @Param("sellerId") Long sellerId, Pageable pageable);
    
    // 상품 삭제(다중)
    @Query("SELECT p FROM Product p WHERE p.productId IN (:ids)")
    int deleteProductsByIds(List<Long> productIds);
    
    // 판매자가 등록한 상품 존재 유무 확인
    @Query("SELECT COUNT(p) FROM Product p WHERE p.sellerId =:sellerId")
    int existsBySellerId(Long sellerId);
}
