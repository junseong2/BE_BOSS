package com.onshop.shop.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProductsDTO {
    
    private Long productId;
    private String name;
    private Integer price;
    private String description;
    private String categoryName;
    private Long stock;

 
   
}