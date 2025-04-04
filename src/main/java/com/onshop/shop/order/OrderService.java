package com.onshop.shop.order;

import java.util.List;

public interface OrderService {
    Order createOrder(OrderDTO orderDTO);
    Order getOrderById(Long orderId);
    
    
    /** 판매자 */
    SellerOrderResponseDTO getOrders(int page, int size, String search, String orderStatus, String paymentStatus, Long userId);
    

    List<OrderResponseDTO> getOrdersByUserId(Long userId);

}