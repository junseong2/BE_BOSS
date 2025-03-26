package com.onshop.shop.order;

public interface OrderService {
    Order createOrder(OrderDTO orderDTO);
    Order getOrderById(Long orderId);
    
    
    /** 판매자 */
    SellerOrderResponseDTO getOrders(int page, int size, String search, String status);
    
}
