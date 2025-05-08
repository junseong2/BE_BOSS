package com.onshop.shop.domain.cart.service;

import java.util.List;

import com.onshop.shop.domain.cart.dto.CartDTO;
import com.onshop.shop.domain.cart.dto.CartItemRequestDTO;
import com.onshop.shop.domain.cart.dto.CartTotalCountResponseDTO;
import com.onshop.shop.domain.cart.entity.Cart;

public interface CartService {

    List<CartDTO> getCartByUserId(Long userId);

    Cart addItemToCart(Long userId, CartItemRequestDTO request);

    boolean updateItemQuantity(Long userId, Long productId, Integer quantity);

    boolean removeItemFromCart(Long cartId);

    boolean removeItemFromCartbyProductId(Long userId, Long productId);

    void clearCart(Long userId);
    
    CartTotalCountResponseDTO totalCountByUserId(Long userId);
}
