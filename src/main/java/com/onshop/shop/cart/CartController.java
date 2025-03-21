package com.onshop.shop.cart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.security.JwtUtil;
@RestController
@RequestMapping("/cart")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // ✅ CORS 설정
public class CartController {

    private final CartService cartService;
    private final JwtUtil jwtUtil;

    public CartController(CartService cartService, JwtUtil jwtUtil) {
        this.cartService = cartService;
        this.jwtUtil = jwtUtil;
    }
    private static final Logger log = LoggerFactory.getLogger(CartController.class); // ✅ 직접 Logger 선언

    // ✅ 장바구니 조회 (쿠키 기반 JWT 인증)
    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(@CookieValue(name = "jwt", required = false) String jwtToken) {
        log.info("🛒 장바구니 조회 요청 수신!");

    	if (jwtToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "JWT 토큰이 없습니다."));
        }

        Long userId = jwtUtil.extractUserId(jwtToken);
        List<CartDTO> cartItems = cartService.getCartByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("cartItems", cartItems);
        response.put("message", "장바구니 조회 성공");
        
        System.out.println("✅ Extracted UserId: " + cartItems);

        System.out.println("✅ Extracted UserId: " + userId);

        log.info("✅ Extracted UserId: \" + cartItems");

        return ResponseEntity.ok(response);
    }

    // ✅ 장바구니에 상품 추가 (쿠키 기반 JWT 인증)
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addItemToCart(
            @CookieValue(name = "jwt", required = false) String jwtToken,
            @RequestBody CartItemRequest request) {

      System.out.println("카트에 추가 "+request);
    	
    	
        if (jwtToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "JWT 토큰이 없습니다."));
        }

        // ✅ JWT에서 userId를 추출 (클라이언트에서 userId를 직접 받지 않음)
        Long userId = jwtUtil.extractUserId(jwtToken);

        if (request.getProductId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "productId가 누락되었습니다."));
        }

        Cart cart = cartService.addItemToCart(userId, request);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "장바구니에 추가되었습니다.");
        response.put("data", Map.of(
            "cartId", cart.getCartId(),
            "productId", request.getProductId(),
            "quantity", request.getQuantity()
        ));

        return ResponseEntity.ok(response);
    }

 // ✅ 장바구니에 상품 수량조절 (쿠키 기반 JWT 인증)
    @PutMapping("/updatequantity")
    public ResponseEntity<Map<String, Object>> updateItemQuantity(
            @CookieValue(name = "jwt", required = false) String jwtToken,
            @RequestBody CartItemRequest request) {

System.out.println("업데이트양");
// JWT 토큰체크
        if (jwtToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "JWT 토큰이 없습니다."));
        }

        // JWT에서 userId 추출
        Long userId = jwtUtil.extractUserId(jwtToken);

        if (request.getProductId() == null || request.getQuantity() == null || request.getQuantity() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "잘못된 요청입니다. productId와 quantity를 확인해주세요."));
        }

        // 장바구니에서 아이템 수량 업데이트
        boolean updated = cartService.updateItemQuantity(userId, request.getProductId(), request.getQuantity());

        if (!updated) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "아이템을 찾을 수 없거나 수량 업데이트에 실패했습니다."));
        }

        // 갱신된 장바구니 정보 가져오기
        List<CartDTO> updatedCart = cartService.getCartByUserId(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("cartItems", updatedCart);
        response.put("message", "장바구니 수량이 갱신되었습니다.");

        return ResponseEntity.ok(response);
    }



    // ✅ 장바구니에서 아이템 제거 (쿠키 기반 JWT 인증)
    @DeleteMapping("/remove")
    public ResponseEntity<Map<String, Object>> removeItemFromCart(
            @CookieValue(name = "jwt", required = false) String jwtToken,
            @RequestParam("productId") Long productId) {

        // JWT 토큰이 없으면 401 Unauthorized 응답
        if (jwtToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "JWT 토큰이 없습니다."));
        }

        // JWT에서 userId 추출
        Long userId = jwtUtil.extractUserId(jwtToken);
        System.out.println("🔍 삭제 요청: userId=" + userId + ", productId=" + productId); // ✅ 디버깅 추가

        // 장바구니에서 아이템 삭제
        boolean removed = cartService.removeItemFromCartbyProductId(userId,productId) ;  // 아이템 제거 서비스 호출

        if (!removed) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "아이템을 찾을 수 없거나 삭제에 실패했습니다."));
        }

        // 장바구니 상태 갱신 후 반환
        List<CartDTO> updatedCart = cartService.getCartByUserId(userId);  // 갱신된 장바구니 정보 가져오기

        Map<String, Object> response = new HashMap<>();
        response.put("cartItems", updatedCart);
        response.put("message", "아이템이 장바구니에서 성공적으로 제거되었습니다.");

        return ResponseEntity.ok(response);
    }


    // ✅ 장바구니 비우기 (쿠키 기반 JWT 인증)
    @PostMapping("/clear")
    public ResponseEntity<Map<String, Object>> clearCart(@CookieValue(name = "jwt", required = false) String jwtToken) {
        if (jwtToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "JWT 토큰이 없습니다."));
        }

        Long userId = jwtUtil.extractUserId(jwtToken);
        cartService.clearCart(userId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "장바구니가 비워졌습니다.");
        return ResponseEntity.ok(response);
    }
}
