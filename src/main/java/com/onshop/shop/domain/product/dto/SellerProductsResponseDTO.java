package com.onshop.shop.domain.product.dto;

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
