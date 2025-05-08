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

/**
 * Payment 엔티티에 대한 JPA 리포지토리입니다.
 * 이 리포지토리는 결제 관련 통계 및 판매자 정보를 관리하는 메서드를 제공합니다.
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    /**
     * 주어진 주문 ID에 해당하는 결제 정보를 반환합니다.
     *
     * @param orderId 주문 ID
     * @return 결제 정보, 존재하지 않으면 {@link Optional#empty()}
     */
    Optional<Payment> findByOrder_OrderId(Long orderId);

    /**
     * 주어진 impUid에 해당하는 결제 정보를 반환합니다.
     *
     * @param impUid 결제 고유 ID
     * @return 결제 정보, 존재하지 않으면 {@link Optional#empty()}
     */
    Optional<Payment> findByImpUid(String impUid);

    /**
     * 판매자의 매출 관리 통계를 반환합니다.
     * 주어진 기간 동안 결제 완료된 주문 건수, 총 매출, 결제 실패 또는 취소된 금액을 포함합니다.
     *
     * @param sellerId 판매자 ID
     * @param startDate 통계 시작 날짜
     * @param endDate 통계 종료 날짜
     * @return 판매자의 결제 통계 정보 {@link SellerPaymentStatisticsDTO}
     */
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

    /**
     * 판매자의 월별 매출 통계를 반환합니다.
     * 'PAID' 상태의 결제만 포함되며, 주어진 기간 동안의 매출을 월별로 집계합니다.
     *
     * @param sellerId 판매자 ID
     * @param startDate 통계 시작 날짜
     * @param endDate 통계 종료 날짜
     * @return 판매자의 월별 매출 {@link SellerPaymentsDTO} 리스트
     */
    @Query("""
        SELECT new com.onshop.shop.domain.payment.dto.SellerPaymentsDTO(
            CONCAT(YEAR(o.createdDate), '년 ', MONTH(o.createdDate), '월'),
            SUM(od.totalPrice)
        )
        FROM Payment p
        LEFT JOIN p.order o
        LEFT JOIN OrderDetail od ON o.orderId = od.order.orderId
        LEFT JOIN od.product pr
        WHERE pr.seller.sellerId = :sellerId
        AND p.paidDate BETWEEN :startDate AND :endDate
        AND p.paymentStatus = 'PAID'
        GROUP BY o.createdDate
        ORDER BY YEAR(o.createdDate), MONTH(o.createdDate)
    """)
    List<SellerPaymentsDTO> getMonthlySalesBySellerOnlyPaid(
        @Param("sellerId") Long sellerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * 판매자의 카테고리별 매출 비율을 반환합니다.
     * 'PAID' 상태의 결제만 포함되며, 주어진 기간 동안의 매출을 카테고리별로 집계합니다.
     *
     * @param sellerId 판매자 ID
     * @param startDate 통계 시작 날짜
     * @param endDate 통계 종료 날짜
     * @return 판매자의 카테고리별 매출 {@link SellerCategorySalesDTO} 리스트
     */
    @Query("""
        SELECT new com.onshop.shop.domain.payment.dto.SellerCategorySalesDTO(
            c.name,
            SUM(od.totalPrice)
        )
        FROM Payment p
        JOIN p.order o
        JOIN OrderDetail od ON o.orderId = od.order.orderId
        JOIN od.product pr
        JOIN pr.category c
        WHERE pr.seller.sellerId = :sellerId
        AND p.paidDate BETWEEN :startDate AND :endDate
        AND p.paymentStatus = com.onshop.shop.domain.payment.enums.PaymentStatus.PAID
        GROUP BY c.name
        ORDER BY SUM(od.totalPrice) DESC
    """)
    List<SellerCategorySalesDTO> getCategorySalesBySeller(
        @Param("sellerId") Long sellerId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );
}
