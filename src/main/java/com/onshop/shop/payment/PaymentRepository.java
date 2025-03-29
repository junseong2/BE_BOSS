package com.onshop.shop.payment;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_OrderId(Long orderId);
    Optional<Payment> findByImpUid(String impUid);
    
    /** 판매자*/
	// 판매자 매출관리 통계(카드): 총 매출, 총 주문건수, 결제완료주문건수, 취소/환불 금액
	@Query("""
		    SELECT new com.onshop.shop.payment.SellerPaymentStatisticsDTO(
		        COALESCE(SUM(CASE WHEN p.paymentStatus = 'PAID' THEN o.totalPrice ELSE 0 END), 0),
		        COUNT(DISTINCT o.orderId),
		        COUNT(DISTINCT CASE WHEN p.paymentStatus = 'PAID' THEN o.orderId END),
		        COALESCE(SUM(CASE WHEN o.status = 'FAILED' OR p.paymentStatus = 'FAILED' THEN o.totalPrice ELSE 0 END), 0)
		    )
		    FROM OrderDetail od
		    JOIN od.order o
		    JOIN od.product pr
		    JOIN Payment p ON p.order.orderId = o.orderId
		    WHERE pr.seller.sellerId = :sellerId
		    AND o.createdDate BETWEEN :startDate AND :endDate
		""")
		SellerPaymentStatisticsDTO findOrderStatsBySellerId(
		    @Param("sellerId") Long sellerId,
		    @Param("startDate") LocalDateTime startDate,
		    @Param("endDate") LocalDateTime endDate
		);
	
	 // 판매자 월별 매출 통계
	@Query("SELECT new com.onshop.shop.payment.SellerPaymentsDTO(" +
            "CONCAT(YEAR(o.createdDate), '년 ', MONTH(o.createdDate), '월'), " +
            "SUM(od.totalPrice)) " +
            "FROM Payment p " +
            "JOIN p.order o " +  // Payment와 Order를 조인
            "JOIN OrderDetail od ON o.orderId = od.order.orderId " +  // Order와 OrderDetail을 외래키로 조인
            "JOIN od.product pr " +  // OrderDetail과 Product를 조인
            "WHERE pr.seller.sellerId = :sellerId " + // 각 판매자별 매출 통계
            "AND o.createdDate BETWEEN :startDate AND :endDate " +
            "AND p.paymentStatus = com.onshop.shop.payment.PaymentStatus.PAID " +
            "GROUP BY o.createdDate  " +
            "ORDER BY YEAR(o.createdDate), MONTH(o.createdDate)")
	List<SellerPaymentsDTO> getMonthlySalesBySellerOnlyPaid(@Param("sellerId") Long sellerId,
                                                @Param("startDate") LocalDateTime startDate,
                                                @Param("endDate") LocalDateTime endDate);

}
