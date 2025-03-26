package com.onshop.shop.order;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderDTO orderDTO);
    Order getOrderById(Long orderId);
    
    List<OrderResponseDTO> getOrdersByUserId(Long userId);

}
