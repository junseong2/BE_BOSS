package com.onshop.shop.domain.order.service;

import java.util.List;

import com.onshop.shop.domain.order.dto.OrderDTO;
import com.onshop.shop.domain.order.dto.OrderResponseDTO;
import com.onshop.shop.domain.order.entity.Order;

public interface OrderBuyerService {
    Order createOrder(OrderDTO orderDTO);
    Order getOrderById(Long orderId);
    List<OrderResponseDTO> getOrdersByUserId(Long userId);
}
