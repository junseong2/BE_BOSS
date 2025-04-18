package com.onshop.shop.orderDetail;

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
