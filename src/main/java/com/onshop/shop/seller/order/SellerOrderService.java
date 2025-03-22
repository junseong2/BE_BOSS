package com.onshop.shop.seller.order;

import java.util.List;

import com.onshop.shop.inventory.InventoryOrderRequestDTO;

public interface SellerOrderService {
	
	
	void createOrder(List<InventoryOrderRequestDTO>  orderDTOs); // 재고 발주

}
