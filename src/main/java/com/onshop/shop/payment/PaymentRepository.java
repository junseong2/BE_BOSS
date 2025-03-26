package com.onshop.shop.payment;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrder_OrderId(Long orderId);
    Optional<Payment> findByImpUid(String impUid);
    
    /** 판매자*/
    // 결제 내역 조회
    @Query("SELECT new com.onshop.shop.payment.SellerPaymentsDTO(p.impUid, u.username, p.paidDate, p.paymentMethod, p.totalAmount, p.paymentStatus) " +
    	       "FROM Payment p " +
    	       "JOIN User u ON p.userId = u.userId " +
    	       "JOIN Order o ON p.order.orderId = o.orderId " +
    	       "JOIN OrderDetail od ON o.orderId = od.order.orderId " +
    	       "JOIN Product pr ON od.product.productId = pr.productId " +
    	       "WHERE pr.seller.sellerId = :sellerId " + 
    	       "AND u.username LIKE CONCAT('%', :search, '%') " +
    	       "GROUP BY p.impUid, u.username, p.paidDate, p.paymentMethod, p.totalAmount, p.paymentStatus")
    	Page<SellerPaymentsDTO> findPaymentDetailsBySellerIdAndSearchAndStatus(
    	        @Param("sellerId") Long sellerId, 
    	        @Param("search") String search,
    	        Pageable pageable);

    
    
    // 결제 내역 카운트
    @Query("SELECT COUNT(p) " +
 	       "FROM Payment p " +
 	       "JOIN User u ON p.userId = u.userId " +
 	       "JOIN Order o ON p.order.orderId = o.orderId " +
 	       "JOIN OrderDetail od ON o.orderId = od.order.orderId " +
 	       "JOIN Product pr ON od.product.productId = pr.productId " +
 	       "WHERE pr.seller.sellerId = :sellerId " + 
 	       "AND u.username LIKE CONCAT('%', :search, '%') " +
 	       "GROUP BY p.impUid, u.username, p.paidDate, p.paymentMethod, p.totalAmount, p.paymentStatus")
 	Long countBySellerIdAndSearchAndStatus(
 	        @Param("sellerId") Long sellerId, 
 	        @Param("search") String search);
		
}
