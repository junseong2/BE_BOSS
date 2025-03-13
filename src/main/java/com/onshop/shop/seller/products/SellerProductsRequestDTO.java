package com.onshop.shop.seller.products;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SellerProductsRequestDTO {
	
	@NotEmpty(message = "상품이름은 필수입니다.")
	private String name;
	
	@NotEmpty(message = "카테고리 선택은 필수입니다.")
	private String categoryName;// 상품 카테고리

	
	@NotEmpty(message = "상품 설명은 필수입니다.")
	private String description;// 상품 설명
	
	@NotNull(message = "가격은 필수입니다.")
	@Positive(message = "가격은 0보다 커야 합니다.")
	private Integer price; 
	
	private Integer stock; // 재고
	
	public SellerProductsRequestDTO(String name, String productName,String description,	String categoryName, Integer price) {
		this.name = name;
		this.description = description;
		this.categoryName = categoryName;
		this.price = price;
		this.stock = 0;
	}
}
