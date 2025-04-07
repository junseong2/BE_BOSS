package com.onshop.shop.inventory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SellerInventoryDTO {
	private Long productId;
	private Long inventoryId;
	private String name;
	private String category;
	private Long stock;
	private Long minStock;
	private java.util.Date updatedDate;

}
