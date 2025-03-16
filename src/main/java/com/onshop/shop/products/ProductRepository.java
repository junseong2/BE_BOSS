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
    List<Product> findBySellerId(Long sellerId);

    
    @Query("SELECT p FROM Product p JOIN p.category c WHERE p.name LIKE %:query% OR c.name LIKE %:query%")
    public List<Product> searchByNameOrCategory(@Param("query") String query);
    
    
    @Query("SELECT new com.onshop.shop.seller.products.SellerProductsDTO( " +
    	       "p.productId, p.name, p.price, c.name, p.description, i.stock, GROUP_CONCAT(pi.imageUrl)) " +
    	       "FROM Product p " +
    	       "JOIN p.category c " +
    	       "LEFT JOIN Inventory i ON i.product.productId = p.productId " +  // Inventory와 Product를 외래키로 연결
    	       "LEFT JOIN ProductImage pi ON pi.product.productId = p.productId " +
    	       "WHERE p.sellerId = :sellerId " + 
    	       "GROUP BY p.productId")
    	List<SellerProductsDTO> findBySellersId(@Param("sellerId") Long sellerId, Pageable pageable);


    
    // 점주 전용 상품 조회(단일)
    @Query("SELECT p FROM Product p WHERE p.sellerId = :sellerId AND p.productId = :productId")
    Product findBySellerIdAndProductId(@Param("sellerId") Long sellerId, @Param("productId") Long productId);
    
    

    // 점주 전용 상품 검색
    @Query(value = ""+
    		"SELECT p.product_id AS productId, p.name AS name, p.price AS price, " +
            "c.category_name AS categoryName, p.description AS description, i.stock AS stock, " +
            "GROUP_CONCAT(pi.image_url) AS imageUrls " + // 여러 이미지 URL을 하나의 문자열로 변환
            "FROM product p " +
            "JOIN category c ON c.category_id = p.category_id " + 
            "LEFT JOIN inventory i ON i.product_id = p.product_id " + 
            "LEFT JOIN product_image pi ON pi.product_id = p.product_id " + // 이미지가 없는 경우도 고려
            "WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :name, '%')" +
            "GROUP BY p.product_id",  // 상품별로 그룹화
    nativeQuery = true)
    Page<SellerProductsDTO> findByNameAndSellerId(@Param("name") String name, @Param("sellerId") Long sellerId, Pageable pageable);
    
    // 상품 삭제(다중)
    @Query("SELECT p FROM Product p WHERE p.productId IN (:ids)")
    int deleteProductsByIds(List<Long> productIds);
    
    // 판매자가 등록한 상품 존재 유무 확인
    @Query("SELECT COUNT(p) FROM Product p WHERE p.sellerId =:sellerId")
    int existsBySellerId(Long sellerId);
}
