package com.onshop.shop.seller.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;


@AllArgsConstructor
@Data
@Builder
public class SellerProductsResponseDTO {
	
	private String productName;
	private String category;// 상품 카테고리
	private String description;// 상품 설명
	private Integer stock;
	private Integer price; 
	

}
