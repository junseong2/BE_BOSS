package com.onshop.shop.seller.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class InventoryOrderRequestDTO {
	
	private Long productId;
	private Long orderStock; // 발주 요청 재고
	private Long currentStock; // 현재 재고
	private Long minStock; // 최소 재고
}
