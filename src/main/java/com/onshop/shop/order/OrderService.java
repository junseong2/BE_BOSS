package com.onshop.shop.order;

public interface OrderService {
    Order createOrder(OrderDTO orderDTO);
    Order getOrderById(Long orderId);
}
