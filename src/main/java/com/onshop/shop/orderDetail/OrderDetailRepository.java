package com.onshop.shop.orderDetail;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.onshop.shop.order.SellerOrderDTO;
import com.onshop.shop.payment.SellerPaymentStatisticsDTO;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

	// 주문 조회(요약)
	@Query("""
			    SELECT new com.onshop.shop.order.SellerOrderDTO(
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
	List<SellerOrderDTO> findOrderSummaryBySellerIdAndStatus(@Param("sellerId") Long sellerId,
			@Param("search") String search, @Param("orderStatus") String orderStatus,
			@Param("paymentStatus") String paymentStatus, Pageable pageable);
	
	
	
	// 판매자 주문 상세 조회

		@Query("""
			    SELECT new com.onshop.shop.orderDetail.SellerOrderDetailResponseDTO(
			        o.orderId, 
			        o.createdDate, 
			        COALESCE(SUM(od.quantity), 0),
			        p.totalAmount - (p.totalAmount * 0.1) + 3000,
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
			             u.username, u.phone1, u.phone2, u.phone3, ad.address1, ad.address2, 
			             ad.post
			    """)
			SellerOrderDetailResponseDTO findOrderDetailsByOrderId(@Param("orderId") Long orderId, @Param("sellerId") Long sellerId);
	


//	@Query("""
//			SELECT new com.onshop.shop.orderDetail.OrderDetailResponseDTO(
//			    o.orderId,
//			    MAX(o.createdDate),
//			    COALESCE(SUM(od.quantity), 0),
//			    COALESCE(p.totalAmount, 0) - (COALESCE(p.totalAmount, 0) * 0.1) + 3000,
//			    MAX(p.paidDate),
//			    p.paymentMethod,
//			    u.username,
//			    CONCAT(COALESCE(u.phone1, ''), '-', COALESCE(u.phone2, ''), '-', COALESCE(u.phone3, '')),
//			    CONCAT('[', COALESCE(ad.post, ''), '] ', COALESCE(ad.address1, ''), ' ', COALESCE(ad.address2, '')),
//			    FUNCTION('GROUP_CONCAT', CONCAT(pr.name, ':::', pr.gImage))
//			)
//			FROM OrderDetail od
//			JOIN od.order o
//			JOIN o.user u
//			JOIN od.product pr
//			LEFT JOIN Payment p ON p.order.orderId = o.orderId
//			LEFT JOIN Address ad ON u.userId = ad.user.userId AND ad.isDefault = true
//			WHERE o.orderId = :orderId
//			GROUP BY
//			    o.orderId,
//			    p.paymentMethod,
//			    p.totalAmount,
//			    p.paidDate,
//			    u.username,
//			    u.phone1,
//			    u.phone2,
//			    u.phone3,
//			    ad.post,
//			    ad.address1,
//			    ad.address2
//			""")
//			OrderDetailResponseDTO findDetailByOrderId(@Param("orderId") Long orderId);
		

	@Query("""
		    SELECT new com.onshop.shop.orderDetail.OrderDetailResponseDTO(
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

	@Query("""
		    SELECT new com.onshop.shop.orderDetail.ProductItemDTO(
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
