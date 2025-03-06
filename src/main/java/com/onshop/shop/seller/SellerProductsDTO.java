package com.onshop.shop.seller;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerProductsDTO {
	
    private Long productId;
    private String name;
    private BigDecimal price;
    private String category;
    private Integer stock;
	
}
