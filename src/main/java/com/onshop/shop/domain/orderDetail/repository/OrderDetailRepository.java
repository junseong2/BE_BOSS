package com.onshop.shop.domain.orderDetail.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.onshop.shop.domain.order.dto.SellerOrderDTO;
import com.onshop.shop.domain.orderDetail.dto.OrderDetailResponseDTO;
import com.onshop.shop.domain.orderDetail.dto.ProductItemDTO;
import com.onshop.shop.domain.orderDetail.dto.SellerOrderDetailResponseDTO;
import com.onshop.shop.domain.orderDetail.entity.OrderDetail;

/**
 * 주문 상세(OrderDetail) 엔티티의 데이터베이스 접근을 담당하는 Repository
 * - 주문 요약 조회
 * - 주문 상세 정보 조회
 * - 판매자 기준 주문 내역 조회 등
 */
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    /**
     * 판매자의 주문 요약 목록 조회
     *
     * @param sellerId       판매자 ID
     * @param search         사용자 이름 검색 키워드
     * @param orderStatus    주문 상태 필터 (LIKE 검색)
     * @param paymentStatus  결제 상태 필터 (LIKE 검색)
     * @param pageable       페이징 정보
     * @return 판매자 주문 요약 DTO 목록
     */
    @Query("""
        SELECT new com.onshop.shop.domain.order.dto.SellerOrderDTO(
            o.orderId,
            u.username,
            o.createdDate,
            COALESCE(SUM(od.quantity), 0),
            p.paymentMethod,
            o.totalPrice,
            p.paymentStatus,
            o.status
        )
        FROM OrderDetail od
        JOIN od.order o
        JOIN o.user u
        JOIN od.product pr
        JOIN Payment p ON p.order.orderId = o.orderId
        WHERE pr.seller.sellerId = :sellerId
        AND CAST(o.status AS string) LIKE %:orderStatus%
        AND CAST(p.paymentStatus AS string) LIKE %:paymentStatus%
        AND u.username LIKE %:search%
        GROUP BY o.orderId, u.username, o.createdDate, p.paymentMethod, p.paymentStatus, o.totalPrice, o.status
        ORDER BY o.createdDate DESC
    """)
    List<SellerOrderDTO> findOrderSummaryBySellerIdAndStatus(
        @Param("sellerId") Long sellerId,
        @Param("search") String search,
        @Param("orderStatus") String orderStatus,
        @Param("paymentStatus") String paymentStatus,
        Pageable pageable
    );

    /**
     * 특정 판매자 기준 주문 상세 내역 조회
     *
     * @param orderId  주문 ID
     * @param sellerId 판매자 ID
     * @return 판매자 주문 상세 응답 DTO
     */
    @Query("""
        SELECT new com.onshop.shop.domain.orderDetail.dto.SellerOrderDetailResponseDTO(
            o.orderId, 
            o.createdDate, 
            COALESCE(SUM(od.quantity), 0),
            p.totalAmount,
            p.paidDate, 
            p.paymentMethod, 
            u.username, 
            CONCAT(u.phone1, '-', u.phone2, '-', u.phone3), 
            CONCAT('[', COALESCE(ad.post, ''), ']', COALESCE(ad.address1, ''), ' ', COALESCE(ad.address2, '')),
            FUNCTION('GROUP_CONCAT', pr.name)
        )
        FROM OrderDetail od
        JOIN od.order o
        JOIN o.user u
        JOIN od.product pr
        JOIN Payment p ON p.order.orderId = o.orderId
        LEFT JOIN Address ad ON u.userId = ad.user.userId AND ad.isDefault = true
        WHERE o.orderId = :orderId AND pr.seller.sellerId = :sellerId
        GROUP BY o.orderId, o.createdDate, p.totalAmount, p.paidDate, p.paymentMethod, 
                 u.username, u.phone1, u.phone2, u.phone3, ad.address1, ad.address2, ad.post
    """)
    SellerOrderDetailResponseDTO findOrderDetailsByOrderId(
        @Param("orderId") Long orderId,
        @Param("sellerId") Long sellerId
    );

    /**
     * 주문 ID로 전체 주문 메타 정보 조회 (관리자 또는 사용자용)
     *
     * @param orderId 주문 ID
     * @return 주문 상세 응답 DTO
     */
    @Query("""
        SELECT new com.onshop.shop.domain.orderDetail.dto.OrderDetailResponseDTO(
            o.orderId,
            o.createdDate,
            SUM(od.quantity),
            p.totalAmount + (p.totalAmount * 0.1) + 3000,
            p.paidDate,
            p.paymentMethod,
            u.username,
            CONCAT(u.phone1, '-', u.phone2, '-', u.phone3),
            CONCAT('[', COALESCE(ad.post, ''), ']', COALESCE(ad.address1, ''), ' ', COALESCE(ad.address2, ''))
        )
        FROM OrderDetail od
        JOIN od.order o
        JOIN o.user u
        LEFT JOIN Payment p ON p.order.orderId = o.orderId
        LEFT JOIN Address ad ON u.userId = ad.user.userId AND ad.isDefault = true
        WHERE o.orderId = :orderId
        GROUP BY o.orderId, o.createdDate, p.totalAmount, p.paidDate, p.paymentMethod,
                 u.username, u.phone1, u.phone2, u.phone3, ad.address1, ad.address2, ad.post
    """)
    OrderDetailResponseDTO findOrderMetaByOrderId(@Param("orderId") Long orderId);

    /**
     * 주문 ID 기준으로 포함된 개별 상품 목록 조회
     *
     * @param orderId 주문 ID
     * @return 주문된 상품 목록 (이름, 이미지, 수량, 총 가격 포함)
     */
    @Query("""
        SELECT new com.onshop.shop.domain.orderDetail.dto.ProductItemDTO(
            pr.name,
            pr.gImage,
            od.quantity,
            od.totalPrice
        )
        FROM OrderDetail od
        JOIN od.product pr
        WHERE od.order.orderId = :orderId
    """)
    List<ProductItemDTO> findOrderProductsByOrderId(@Param("orderId") Long orderId);
}
