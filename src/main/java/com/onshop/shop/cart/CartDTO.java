package com.onshop.shop.cart;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartDTO {
    private Long cartId;
    private Long productId;
    private int quantity;
    private int userId;
    private String productName; // ✅ product에서 상품명 추가
    private int productPrice;   // ✅ product에서 상품 가격 추가
}