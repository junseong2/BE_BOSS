package com.onshop.shop.seller.inventory;

import java.util.List;

public interface SellerInventoryService {
	
	
	List<SellerInventoryResponseDTO> getAllInventory(int page, int size);
	
	
	void updateInventory(List<InventoryOrderRequestDTO> orderRequests); // 재고 발주 및 즉시 반영

}
