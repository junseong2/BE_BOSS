package com.onshop.shop.seller;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
public class SellerProductsDTO {
    
    private Long productId;
    private String name;
    private Integer price;
    private String categoryName;
    private Long stock;
    
    
    public SellerProductsDTO(Long productId, String name, Integer price, String categoryName, Long stock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.categoryName = categoryName;
        this.stock = stock;
    }
    
}
