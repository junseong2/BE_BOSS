package com.onshop.shop.cart;

import java.util.List;

public interface CartService {

    List<CartDTO> getCartByUserId(Long userId);

    Cart addItemToCart(Long userId, CartItemRequest request);

    boolean updateItemQuantity(Long userId, Long productId, Integer quantity);

    boolean removeItemFromCart(Long cartId);

    boolean removeItemFromCartbyProductId(Long userId, Long productId);

    void clearCart(Long userId);
    
    CartTotalCountResponseDTO totalCountByUserId(Long userId);
}
