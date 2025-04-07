package com.onshop.shop.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerProductsListDTO {
    
    private Long productId;
    private String name;
    private Integer price;
    private String categoryName;
    private String description;
    private Long stock;
    private String gImage; 
    

   
}