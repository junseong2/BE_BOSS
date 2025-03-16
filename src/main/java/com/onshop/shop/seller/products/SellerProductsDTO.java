package com.onshop.shop.seller.products;

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
    private String description;
    private String categoryName;
    private Long stock;


    public SellerProductsDTO(Long productId, String name, Integer price, String categoryName, String description, Long stock) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.description = description;
        this.categoryName = categoryName;
        this.stock = stock;
    }

}
