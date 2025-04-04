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

//	 주문 상세 조회
	@Query(value = """
			SELECT 
			    o.order_id AS orderId,
			    o.created_date AS createdDate,
			    SUM(od.quantity) AS quantity,
			    p.total_amount - (p.total_amount * 0.1) + 3000 AS totalAmount,
			    p.paid_date AS paidDate,
			    p.payment_method AS paymentMethod,
			    u.username AS username,
			    CONCAT(u.phone1, '-', u.phone2, '-', u.phone3) AS phoneNumber,
			    CONCAT('[', IFNULL(ad.post, ''), '] ', IFNULL(ad.address1, ''), ' ', IFNULL(ad.address2, '')) AS address,
			    GROUP_CONCAT(pr.name) AS productNames,
			    GROUP_CONCAT(pr.g_image) AS productImages
			FROM order_detail od
			JOIN orders o ON od.order_id = o.order_id
			JOIN user u ON o.user_id = u.user_id
			JOIN product pr ON od.product_id = pr.product_id
			JOIN payment p ON p.order_id = o.order_id
			LEFT JOIN address ad ON u.user_id = ad.user_id AND ad.is_default = 1
			WHERE o.order_id = :orderId AND pr.seller_id = :sellerId
			GROUP BY o.order_id
		""", nativeQuery = true)
		OrderDetailResponseDTO findOrderDetailsByOrderId(@Param("orderId") Long orderId, @Param("sellerId") Long sellerId);


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
