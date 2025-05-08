package com.onshop.shop.domain.product.dto;

import java.time.LocalDateTime;

import com.onshop.shop.domain.product.enums.DiscountRate;

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