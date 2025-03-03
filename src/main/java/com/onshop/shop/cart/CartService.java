package com.onshop.shop.cart;

import com.onshop.shop.cart.dto.CartItemRequest;
import com.onshop.shop.cart.model.CartEntity;
import com.onshop.shop.cart.model.CartItemEntity;
import com.onshop.shop.cart.CartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // 사용자 ID로 장바구니 조회
    public CartEntity getCartByUserId(Long userId) {
        CartEntity cart = cartRepository.findByUserId(userId);
        if (cart == null) {
            cart = new CartEntity();
            cart.setUserId(userId);
        }
        return cart;
    }

    // 장바구니에 아이템 추가 (간단한 예시)
    @Transactional
    public CartEntity addItemToCart(Long userId, CartItemRequest request) {
        CartEntity cart = getCartByUserId(userId);
        // 신규 아이템 생성 및 장바구니에 추가
        CartItemEntity item = new CartItemEntity();
        item.setProductId(request.getProductId());
        item.setQuantity(request.getQuantity());
        item.setCart(cart);
        cart.getItems().add(item);
        return cartRepository.save(cart);
    }
}
