package com.onshop.shop.domain.orderDetail.service;


import com.onshop.shop.domain.order.dto.OrderDTO;
import com.onshop.shop.domain.order.entity.Order;
import com.onshop.shop.domain.orderDetail.dto.OrderDetailResponseDTO;
import com.onshop.shop.domain.orderDetail.dto.SellerOrderDetailResponseDTO;

public interface OrderDetailService {
	void createOrderDetail(Long userId, OrderDTO orderDTO, Order order);
	
	// 주문번호 별 주문 상세 내역 조회
	OrderDetailResponseDTO getDetailByOrderId(Long orderId);

	// 주문번호 별 주문 상세 내역 조회
	SellerOrderDetailResponseDTO getOrderDetailByOrderId(Long orderId, Long userId);	
	
	
}
