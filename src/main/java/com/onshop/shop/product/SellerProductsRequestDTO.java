package com.onshop.shop.product;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
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
	
	@Min(value = 0, message = "재고는 최소 0 이상이어야 합니다.")
	@Max(value= 1000, message ="재고는 최대 1000이하이어야 합니다.")
	private Long stock; // 재고
	
	
	
	
}
