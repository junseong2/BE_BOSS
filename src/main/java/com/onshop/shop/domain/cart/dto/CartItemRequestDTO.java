package com.onshop.shop.domain.cart.dto;
import lombok.Data;

@Data
public class CartItemRequestDTO {
	private Long userId;
    private Long productId;
    private Integer quantity;

}
