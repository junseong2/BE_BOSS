package com.onshop.shop.domain.order.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.domain.order.dto.OrderDTO;
import com.onshop.shop.domain.order.entity.Order;
import com.onshop.shop.domain.order.service.OrderBuyerService;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

/**
 * 구매자 주문 컨트롤러
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/orders/buyer")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderBuyerController {

    private final OrderBuyerService orderService;

    // 주문 생성
    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            if (orderDTO.getUserId() == null) {
                throw new IllegalArgumentException("userId가 null입니다!");
            }

            Order order = orderService.createOrder(orderDTO);
            return ResponseEntity.ok(Map.of("orderId", order.getOrderId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("주문 생성 실패: " + e.getMessage());
        }
    }

    // 단일 주문 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }

    // 구매자 주문 목록 조회
    @GetMapping("/user/{userId}")
    @Transactional
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        try {
            var orders = orderService.getOrdersByUserId(userId);

            if (orders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("주문 내역이 없습니다.");
            }

            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("주문 조회 실패: " + e.getMessage());
        }
    }
}
