package com.onshop.shop.inventory;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.onshop.shop.product.Product;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

	// 판매자ID 별 재고 조회
	@Query(value = "SELECT i.product_id AS productId, i.inventory_id AS inventoryId, p.name AS name, c.category_name AS category, i.stock AS stock ,i.min_stock AS minStock, i.updated_date AS updatedDate " +
            "FROM inventory i " +
            "JOIN product p ON i.product_id = p.product_id " +
            "JOIN category c ON c.category_id = p.category_id " + 
            "WHERE p.seller_id = :sellerId " +
            "AND p.name LIKE CONCAT('%', :name, '%') " +
            "AND (" +
            "    (:state = 'soldout' AND i.stock <= 0) OR " + // 재고가 0 일 떄
            "    (:state = 'warn' AND i.stock < i.min_stock) OR " + // 최소 재고 보다 적을 때
            "    (:state = 'all') " +  // 재고0, 최소 재고 미만 모두 조회 때
            ")", 
    nativeQuery = true)
	Page<SellerInventoryDTO> findAllBySellerIdAndSearch(@Param("sellerId") Long sellerId,
                                                    @Param("name") String name,
                                                    @Param("state") String state,
                                                    Pageable pageable);

    
    // 상품 ID에 포함되는 모든 재고 목록 조회    
    @Query("SELECT i FROM Inventory i WHERE i.product.id IN :productIds")
    List<Inventory> findAllByProductIds(@Param("productIds") List<Long> productIds);
    
    // stock(재고 수량) 조회
    @Query(value = "SELECT stock FROM inventory WHERE product_id = :productId", nativeQuery = true)
    Optional<Long> findStockByProductId(@Param("productId") Long productId);
    
    // 판매자별 재고 항목 개수
	@Query(value = "SELECT COUNT(*)" +
            "FROM inventory i " +
            "JOIN product p ON i.product_id = p.product_id " +
            "JOIN category c ON c.category_id = p.category_id " + 
            "WHERE p.seller_id = :sellerId " +
            "AND p.name LIKE CONCAT('%', :name, '%') " +
            "AND (" +
            "    (:state = 'soldout' AND i.stock <= 0) OR " + // 재고가 0 일 떄
            "    (:state = 'warn' AND i.stock < i.min_stock) OR " + // 최소 재고 보다 적을 때
            "    (:state = 'all') " +  // 재고0, 최소 재고 미만 모두 조회 때
            ")", 
    nativeQuery = true)
	Long countBySellerIdAndSearch(@Param("sellerId") Long sellerId,
                                  @Param("name") String name,
                                  @Param("state") String state);
	
	// 상품별 재고 조회
	Inventory findByProduct(Product product);
  

}
