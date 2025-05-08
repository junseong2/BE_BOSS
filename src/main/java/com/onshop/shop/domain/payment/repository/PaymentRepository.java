package com.onshop.shop.domain.payment.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.onshop.shop.domain.payment.dto.SellerCategorySalesDTO;
import com.onshop.shop.domain.payment.dto.SellerPaymentStatisticsDTO;
import com.onshop.shop.domain.payment.dto.SellerPaymentsDTO;
import com.onshop.shop.domain.payment.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_OrderId(Long orderId);
    Optional<Payment> findByImpUid(String impUid);
    
    /** 판매자*/
	// 판매자 매출관리 통계(카드): 총 매출, 총 주문건수, 결제완료주문건수, 취소/환불 금액
    // TODO: 현재 로직은 중복된 데이터를 그룹핑하여 중복 연산을 수행하고 있으므로, 데이터가 커질수록 연산 시간이 늘어나고 성능저하 문제로 이어질 여지가 다분함.
    @Query("""
    	    SELECT new com.onshop.shop.domain.payment.dto.SellerPaymentStatisticsDTO(
    	        COALESCE(SUM(DISTINCT CASE WHEN p.paymentStatus = 'PAID' THEN o.totalPrice ELSE 0 END), 0),
    	        COUNT(DISTINCT o.orderId),
    	        COUNT(DISTINCT CASE WHEN p.paymentStatus = 'PAID' THEN o.orderId END),
    	        COALESCE(SUM(CASE WHEN o.status = 'FAILED' OR p.paymentStatus = 'FAILED' THEN o.totalPrice ELSE 0 END), 0)
    	    )
    	    FROM Order o
    	    JOIN OrderDetail od ON od.order.orderId = o.orderId
    	    JOIN od.product pr
    	    JOIN Payment p ON p.order.orderId = o.orderId
    	    WHERE pr.seller.sellerId = :sellerId
    	    AND p.paidDate BETWEEN :startDate AND :endDate
    	    GROUP BY pr.seller.sellerId
    	""")
    	SellerPaymentStatisticsDTO findOrderStatsBySellerId(
    	    @Param("sellerId") Long sellerId,
    	    @Param("startDate") LocalDateTime startDate,
    	    @Param("endDate") LocalDateTime endDate
    	);

	
	 // 판매자 월별 매출 통계
	@Query("SELECT new com.onshop.shop.domain.payment.dto.SellerPaymentsDTO(" +
            "CONCAT(YEAR(o.createdDate), '년 ', MONTH(o.createdDate), '월'), " +
            "SUM(od.totalPrice)) " +
            "FROM Payment p " +
            "LEFT JOIN p.order o " +  // Payment와 Order를 조인
            "LEFT JOIN OrderDetail od ON o.orderId = od.order.orderId " +  // Order와 OrderDetail을 외래키로 조인
            "LEFT JOIN od.product pr " +  // OrderDetail과 Product를 조인
            "WHERE pr.seller.sellerId = :sellerId " + // 각 판매자별 매출 통계
            "AND p.paidDate BETWEEN :startDate AND :endDate " +
            "AND p.paymentStatus = 'PAID' " +
            "GROUP BY o.createdDate  " +
            "ORDER BY YEAR(o.createdDate), MONTH(o.createdDate)")
	List<SellerPaymentsDTO> getMonthlySalesBySellerOnlyPaid(
												@Param("sellerId") Long sellerId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

	// 판매자 카테고리별 매출 비율
	@Query("SELECT new com.onshop.shop.domain.payment.dto.SellerCategorySalesDTO(" +
		       "c.name, " +
		       "SUM(od.totalPrice)) " +
		       "FROM Payment p " +
		       "JOIN p.order o " +
		       "JOIN OrderDetail od ON o.orderId = od.order.orderId " +
		       "JOIN od.product pr " +
		       "JOIN pr.category c " +  // 상품과 카테고리를 조인
		       "WHERE pr.seller.sellerId = :sellerId " +  
		       "AND p.paidDate BETWEEN :startDate AND :endDate " +
		       "AND p.paymentStatus = com.onshop.shop.domain.payment.enums.PaymentStatus.PAID " +
		       "GROUP BY c.name " + // 카테고리별로 그룹화
		       "ORDER BY SUM(od.totalPrice) DESC")
		List<SellerCategorySalesDTO> getCategorySalesBySeller(
		       @Param("sellerId") Long sellerId,
		       @Param("startDate") LocalDateTime startDate,
		       @Param("endDate") LocalDateTime endDate);

	
}
