package com.onshop.shop.seller.inventory;

import java.util.List;

public interface SellerInventoryService {
	
	
	List<SellerInventoryResponseDTO> getAllInventory(int page, int size);

}
