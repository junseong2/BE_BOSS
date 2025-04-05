package com.onshop.shop.product;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@AllArgsConstructor
@Data
@Builder
public class SellerProductsResponseDTO {
	Long totalCount;
	List<SellerProductsDTO> products;
	

}
