package com.onshop.shop.order;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create")
    public ResponseEntity<?> createOrder(@RequestBody OrderDTO orderDTO) {
        try {
            System.out.println("📩 [DEBUG] 주문 생성 요청: " + orderDTO);
            System.out.println("🔹 userId: " + orderDTO.getUserId());
            System.out.println("🔹 totalPrice: " + orderDTO.getTotalPrice());

            if (orderDTO.getUserId() == null) {
                throw new IllegalArgumentException("❌ userId가 null입니다!");
            }

            Order order = orderService.createOrder(orderDTO);

            System.out.println("✅ 주문 생성 완료! Order ID: " + order.getOrderId());

            return ResponseEntity.ok(Map.of("orderId", order.getOrderId()));
        } catch (Exception e) {
            System.err.println("🔴 주문 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 생성 실패: " + e.getMessage());
        }
    }

    // ✅ 주문 조회 API
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }
}
