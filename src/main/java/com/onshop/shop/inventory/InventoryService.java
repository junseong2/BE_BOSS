package com.onshop.shop.inventory;

import java.util.List;

public interface InventoryService {
	SellerInventoryResponseDTO getAllInventory(int page, int size, String search, String state); // 판매자 인벤토리 조회
	void updateInventory(List<InventoryOrderRequestDTO> orderRequests); // 재고 발주 및 즉시 반영

}
