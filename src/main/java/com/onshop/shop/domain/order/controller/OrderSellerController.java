package com.onshop.shop.domain.order.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.domain.order.dto.SellerOrderResponseDTO;
import com.onshop.shop.domain.order.service.OrderSellerService;
import com.onshop.shop.global.exception.NotAuthException;
import com.onshop.shop.global.util.JwtUtil;

import lombok.RequiredArgsConstructor;

/**
 * 판매자 주문 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders/seller")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderSellerController {

    private final OrderSellerService orderService;
    private final JwtUtil jwtUtil;

    // 판매자 주문 목록 조회
    @GetMapping("/orders")
    public ResponseEntity<?> getSellerOrders(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam String search,
            @RequestParam String orderStatus,
            @RequestParam String paymentStatus,
            @CookieValue(value = "jwt", required = false) String token) {

        if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
        SellerOrderResponseDTO orders = orderService.getOrders(page, size, search, orderStatus, paymentStatus, userId);

        return ResponseEntity.ok(orders);
    }

    // 판매자 주문 상태 변경 (미완성된 로직)
    @PatchMapping("/orders/{orderId}")
    public ResponseEntity<?> updateSellerOrderStatus(
            @PathVariable Long orderId,
            @CookieValue(value = "jwt", required = false) String token) {

        if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
        // TODO: 상태 변경 구현 필요
        return ResponseEntity.ok("판매자 주문 상태 변경 로직 필요");
    }
}
