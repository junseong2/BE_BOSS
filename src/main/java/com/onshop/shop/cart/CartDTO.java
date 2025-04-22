package com.onshop.shop.cart;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CartDTO {
    private Long cartId;
    private int quantity;
	private Long userId;
    private Long productId;
    private String productName; // product에서 상품명 추가
    private int productPrice;   // product에서 상품 가격 추가
    private String productThumbnail;
}