package com.onshop.shop.order;

import java.util.List;

public interface OrderService {
    Order createOrder(Long userId, int totalPrice);
    Order getOrderById(Long orderId);
    List<Order> getOrdersByUser(Long userId);
    void updateOrderStatus(Long orderId, OrderStatus status);
}
