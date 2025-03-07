package com.onshop.shop.cart;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.onshop.shop.products.ProductsService; // ✅ ProductsService 사용
import com.onshop.shop.products.ProductsDTO;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final ProductsService productsService; // ✅ ProductsService 주입

    // ✅ 생성자에서 productsService도 함께 주입
    public CartService(CartRepository cartRepository, ProductsService productsService) {
        this.cartRepository = cartRepository;
        this.productsService = productsService;
    }

    // ✅ 사용자 ID로 장바구니 아이템 조회 
    public List<CartDTO> getCartByUserId(Long userId) {
        List<CartEntity> cartItems = cartRepository.findByUserId(userId);

        return cartItems.stream().map(cart -> {
            // ✅ productsService를 사용하여 product 정보 가져오기
            ProductsDTO productDTO = productsService.getProductById(cart.getProductId());

            return new CartDTO(
                cart.getCartId(),
                cart.getProductId(),
                cart.getQuantity(),
                cart.getUserId(),
                (productDTO != null) ? productDTO.getName() : "상품 없음", // ✅ 상품명 추가
                (productDTO != null) ? productDTO.getPrice() : 0       // ✅ 상품 가격 추가
            );
        }).collect(Collectors.toList());
    }

    // ✅ 장바구니에 상품 추가
    public CartEntity addItemToCart(Long userId, CartItemRequest request) {
        CartEntity cart = new CartEntity();
        cart.setUserId(userId);
        cart.setProductId(request.getProductId());
        cart.setQuantity(request.getQuantity());
        return cartRepository.save(cart);
    }

    // ✅ cartId로 아이템 제거
    public boolean removeItemFromCart(Long cartId) {
        if (cartRepository.existsById(cartId)) {
            cartRepository.deleteById(cartId);
            return true;
        }
        return false;
    }

    // ✅ 장바구니 비우기
    public void clearCart(Long userId) {
        List<CartEntity> carts = cartRepository.findByUserId(userId);
        cartRepository.deleteAll(carts);
    }
}
