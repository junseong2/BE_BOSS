package com.onshop.shop.inventory;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.onshop.shop.seller.inventory.SellerInventoryResponseDTO;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

	// 판매자ID 별 재고 조회
    @Query(value = "SELECT i.product_id AS productId, i.inventory_id AS inventoryId, p.name AS name, c.category_name AS category, i.stock AS stock ,i.min_stock AS minStock, i.updated_date AS updatedDate " +
            "FROM inventory i " +
            "JOIN product p ON i.product_id = p.product_id " +
            "JOIN category c ON c.category_id = p.category_id " + 
            "WHERE p.seller_id = :sellerId", 
    nativeQuery = true)
    Page<SellerInventoryResponseDTO> findAllBySellerId(@Param("sellerId") Long sellerId, Pageable pageable);
    
    // 재고 조회(검색)
    @Query(value = "SELECT i.product_id AS productId, i.inventory_id AS inventoryId, p.name AS name, c.category_name AS category, i.stock AS stock ,i.min_stock AS minStock, i.updated_date AS updatedDate " +
            "FROM inventory i " +
            "JOIN product p ON i.product_id = p.product_id " +
            "JOIN category c ON c.category_id = p.category_id " + 
            "WHERE p.seller_id = :sellerId AND p.name LIKE CONCAT('%', :name, '%')", 
    nativeQuery = true)
    Page<SellerInventoryResponseDTO> findAllByNameAndSellerId(@Param("name") String name, @Param("sellerId") Long sellerId, Pageable pageable);
    
    // 상품ID 별 재고 목록 조회
//    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId ")
    
    
    @Query("SELECT i FROM Inventory i WHERE i.product.id IN :productIds")
    List<Inventory> findAllByProductIds(@Param("productIds") List<Long> productIds);
    
    
    // 상품ID 별 재고 조회
    @Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
    Inventory findByProductId(@Param("productId") Long productId);
    
}
