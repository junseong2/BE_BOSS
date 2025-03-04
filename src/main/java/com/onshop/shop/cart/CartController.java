package com.onshop.shop.cart;

import com.onshop.shop.cart.CartEntity;
import com.onshop.shop.cart.CartService;
import com.onshop.shop.cart.CartItemRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:5173") // 프론트엔드 URL에 맞게 수정
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 사용자별 장바구니 조회
    @GetMapping
    public CartEntity getCart(@RequestParam("userId") Long userId) {
        return cartService.getCartByUserId(userId);
    }

    // 장바구니에 상품 추가
    @PostMapping("/add")
    public CartEntity addItemToCart(@RequestParam("userId") Long userId,
                              @RequestBody CartItemRequest request) {
        return cartService.addItemToCart(userId, request);
    }
}
