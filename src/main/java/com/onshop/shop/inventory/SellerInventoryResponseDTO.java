package com.onshop.shop.inventory;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerInventoryResponseDTO {
	
	private List<SellerInventoryDTO> inventories;
	private Long totalCount;

}
