package com.onshop.shop.cart;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CartService {

    private final CartRepository cartRepository;

    public CartService(CartRepository cartRepository) {
        this.cartRepository = cartRepository;
    }

    // 사용자 ID로 장바구니 아이템 조회
    public List<CartEntity> getCartByUserId(Long userId) {
        return cartRepository.findByUserId(userId);
    }

    // 장바구니에 상품 추가
    public CartEntity addItemToCart(Long userId, CartItemRequest request) {
        CartEntity cart = new CartEntity();
        cart.setUserId(userId);
        cart.setProductId(request.getProductId());
        cart.setQuantity(request.getQuantity());
        return cartRepository.save(cart);
    }

    // cartId로 아이템 제거
    public boolean removeItemFromCart(Long cartId) { // cartId로 수정
        if (cartRepository.existsById(cartId)) {
            cartRepository.deleteById(cartId);
            return true;
        }
        return false;
    }

    // 장바구니 비우기
    public void clearCart(Long userId) {
        List<CartEntity> carts = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(carts);
    }
}
