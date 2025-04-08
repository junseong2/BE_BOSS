package com.onshop.shop.product;

import java.time.LocalDateTime;

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
    private Integer originPrice;
    private String categoryName;
    private String description;
    private LocalDateTime expiryDate;
    private Boolean isDiscount;
    private DiscountRate discountRate;
    private Long stock;
    private String gImage; 
}