package com.onshop.shop.order;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerOrderResponseDTO {
	
	private List<SellerOrderDTO> orders;
	private Long totalCount;
	

}
