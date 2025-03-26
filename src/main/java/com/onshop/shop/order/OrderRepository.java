package com.onshop.shop.order;


import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface OrderRepository extends JpaRepository<Order, Long> {
	
	
	
	/** 판매자*/

   // 판매자별 전체 주문 건수 조회
	@Query("""
		    SELECT COUNT(DISTINCT o.orderId)
		    FROM Order o
		    JOIN OrderDetail od ON o.orderId = od.order.orderId
		    JOIN Product pr ON od.product.productId = pr.productId
		    WHERE pr.seller.sellerId = :sellerId
		""")
		Long countOrdersBySeller(@Param("sellerId") Long sellerId);

}
