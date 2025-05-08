package com.onshop.shop.domain.orderDetail.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductItemDTO {
    private String productName;
    private String productImages;
    private int quantity;
    private Integer price;
}
