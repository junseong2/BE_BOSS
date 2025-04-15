package com.onshop.shop.cart;
import com.onshop.shop.product.Product;

import lombok.Data;

@Data
public class CartItemRequest {
	private Long userId;
    private Long productId;
    private Integer quantity;

}
