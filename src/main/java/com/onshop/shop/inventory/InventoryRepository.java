package com.onshop.shop.inventory;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface InventoryRepository extends JpaRepository<Inventory, Long> {

	// 상품ID 별 재고 조회
	@Query("SELECT i FROM Inventory i WHERE i.product.id = :productId")
	Inventory findByProductId(@Param("productId") Long productId);

}
