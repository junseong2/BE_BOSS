package com.onshop.shop.product;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // 특정 카테고리의 상품 조회
    List<Product> findByCategoryId(Long categoryId);
    List<Product> findByCategoryIdIn(List<Long> categoryIds);

    //sellerId를 엔티티 기준으로 수정 (seller.sellerId → seller.id)
    @Query("SELECT p FROM Product p WHERE p.seller.sellerId = :sellerId")
    List<Product> findBySellerSellerId(@Param("sellerId") Long sellerId);

    // 페이지네이션 적용
    //Page<Product> findBySeller_SellerId(Long sellerId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.seller.sellerId = :sellerId")
    Page<Product> findBySellerSellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    // 상품 이름 또는 카테고리명으로 검색
    @Query("SELECT p FROM Product p JOIN p.category c WHERE p.name LIKE %:query% OR c.name LIKE %:query%")
    List<Product> searchByNameOrCategory(@Param("query") String query);

    // 판매자(점주)의 상품 조회 (Native Query)
    @Query(value = """
        SELECT p.product_id AS productId, p.name AS name, p.price AS price, 
               c.category_name AS categoryName, p.description AS description, i.stock AS stock
        FROM product p
        JOIN category c ON c.category_id = p.category_id
        JOIN inventory i ON i.product_id = p.product_id
        WHERE p.seller_id = :sellerId
    """, nativeQuery = true)
    Page<SellerProductsDTO> findSellerProductsBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);

    // 점주 전용 상품 조회 (단일)
    @Query("SELECT p FROM Product p WHERE p.seller.id = :sellerId AND p.id = :productId")
    Product findBySellerIdAndProductId(@Param("sellerId") Long sellerId, @Param("productId") Long productId);

    // 점주 전용 상품 검색 (Native Query)
    @Query(value = """
        SELECT p.product_id AS productId, p.name AS name, p.price AS price, 
               c.category_name AS categoryName, p.description AS description, i.stock AS stock
        FROM product p
        JOIN category c ON c.category_id = p.category_id
        LEFT JOIN inventory i ON i.product_id = p.product_id
        WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :name, '%')
    """, nativeQuery = true)
    Page<SellerProductsDTO> findByNameAndSellerId(@Param("name") String name, @Param("sellerId") Long sellerId, Pageable pageable);

    // 상품 삭제(다중) - DELETE 쿼리로 변경
    @Modifying
    @Transactional
    @Query("DELETE FROM Product p WHERE p.id IN (:ids)")
    int deleteProductsByIds(@Param("ids") List<Long> productIds);

    // 판매자가 등록한 상품 존재 유무 확인
    @Query("SELECT COUNT(p) FROM Product p WHERE p.seller.id = :sellerId")
    int existsBySellerId(@Param("sellerId") Long sellerId);
}
