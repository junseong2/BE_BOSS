package com.onshop.shop.orderDetail;

import java.util.List;

import com.onshop.shop.order.Order;
import com.onshop.shop.order.OrderDTO;

public interface OrderDetailService {
	void createOrderDetail(Long userId, OrderDTO orderDTO, Order order);
	
	// 주문번호 별 주문 상세 내역 조회
	List<OrderDetailResponseDTO> getOrderDetailByOrderId(Long orderId);
}
