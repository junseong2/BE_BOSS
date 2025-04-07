package com.onshop.shop.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class InventoryOrderRequestDTO {
	private Long productId;
	private Long inventoryId;
	private String category;
	private String name;
	private Long stock; // 현재 재고
	private Long minStock; // 최소 재고
}
