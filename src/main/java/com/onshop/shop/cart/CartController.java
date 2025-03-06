package com.onshop.shop.cart;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping; // DELETE 메서드를 위해 추가
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:5173") // 프론트엔드 URL에 맞게 수정
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    // 장바구니 조회
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(@RequestParam("userId") Long userId) {
        List<CartEntity> carts = cartService.getCartByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("cartItems", carts);

        // 장바구니 아이템 출력
        Set<Long> printedCartIds = new HashSet<>();

        for (CartEntity cart : carts) {
            Long cartId = cart.getCartId();
            Long cartUserId = cart.getUserId();

            if (!printedCartIds.contains(cartId)) {
                System.out.println("장바구니 ID: " + cartId + ", 사용자의 ID: " + cartUserId);
                printedCartIds.add(cartId);
            }

            System.out.println("상품 ID: " + cart.getProductId() + ", 수량: " + cart.getQuantity() + "개");
        }

        response.put("message", "장바구니 조회 성공");
        return ResponseEntity.ok(response);
    }

    // 장바구니에 상품 추가
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addItemToCart(
            @RequestParam("userId") Long userId,
            @RequestBody CartItemRequest request) {

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "장바구니에 추가되었습니다.");

        CartEntity cart = cartService.addItemToCart(userId, request);

        response.put("data", Map.of(
            "cartId", cart.getCartId(),
            "productId", request.getProductId(),
            "quantity", request.getQuantity()
        ));

        return ResponseEntity.ok(response);
    }

    // 장바구니에서 아이템 제거 (cartId로 제거)
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeItemFromCart(
            @RequestParam("cartId") Long cartId) { // cartId로 수정

        Map<String, Object> response = new HashMap<>();
        boolean removed = cartService.removeItemFromCart(cartId); // 서비스 메서드 호출

        if (removed) {
            response.put("success", true);
            response.put("message", "장바구니에서 아이템이 삭제되었습니다.");
        } else {
            response.put("success", false);
            response.put("message", "아이템 삭제 실패: 해당 cartId를 가진 아이템이 없습니다.");
        }

        return ResponseEntity.ok(response);
    }

    // 장바구니 비우기
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCart(@RequestParam("userId") Long userId) {
        cartService.clearCart(userId); // 서비스 메서드 호출

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "장바구니가 비워졌습니다.");
        return ResponseEntity.ok(response);
    }
}
