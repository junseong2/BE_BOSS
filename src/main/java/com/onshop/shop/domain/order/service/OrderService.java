package com.onshop.shop.domain.order.service;

import java.util.List;

import com.onshop.shop.domain.order.dto.OrderDTO;
import com.onshop.shop.domain.order.dto.OrderResponseDTO;
import com.onshop.shop.domain.order.dto.SellerOrderResponseDTO;
import com.onshop.shop.domain.order.entity.Order;

public interface OrderService {
    Order createOrder(OrderDTO orderDTO);
    Order getOrderById(Long orderId);
    
    
    /** 판매자 */
    SellerOrderResponseDTO getOrders(int page, int size, String search, String orderStatus, String paymentStatus, Long userId);
    

    List<OrderResponseDTO> getOrdersByUserId(Long userId);

}
