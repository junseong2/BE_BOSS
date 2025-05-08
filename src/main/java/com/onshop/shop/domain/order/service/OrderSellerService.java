package com.onshop.shop.domain.order.service;

import com.onshop.shop.domain.order.dto.SellerOrderResponseDTO;


public interface OrderSellerService {

    
    /** 판매자 */
    SellerOrderResponseDTO getOrders(int page, int size, String search, String orderStatus, String paymentStatus, Long userId);
    

}
