package com.onshop.shop.seller.inventory;

import java.util.List;

public interface SellerInventoryService {
	List<SellerInventoryResponseDTO> getAllInventory(int page, int size); // 판매자 인벤토리 조회
	List<SellerInventoryResponseDTO> searchInventories(String search, int page, int size); // 재고 상품 검색
	void updateInventory(List<InventoryOrderRequestDTO> orderRequests); // 재고 발주 및 즉시 반영

}
