package com.onshop.shop.products;

import org.apache.ibatis.annotations.Param;
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
    @Query(nativeQuery = true, value =  ""
    		+ "SELECT p.product_id as productId, p.name as name, p.price as price, c.category_name as category, i.stock as stock "
    		+ "FROM product p INNER JOIN inventory i ON p.product_id = i.inventory_id "
    		+ "INNER JOIN category c ON p.product_id = c.category_id")
    public List<SellerProductsDTO> findBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);
}
