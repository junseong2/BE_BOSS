package com.onshop.shop.order;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.exception.NotAuthException;
import com.onshop.shop.orderDetail.OrderDetailService;
import com.onshop.shop.security.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class OrderController {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final JwtUtil jwtUtil;


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
    

    
    @GetMapping("/user/{userId}")
    @Transactional
    public ResponseEntity<?> getOrdersByUserId(@PathVariable Long userId) {
        try {
            System.out.println("📩 [DEBUG] 주문 목록 조회 요청: userId=" + userId);

            // OrderService에서 userId로 주문 목록 가져오기
            var orders = orderService.getOrdersByUserId(userId);

            System.out.println("🔹 조회된 주문 개수: " + orders.size());

            if (orders.isEmpty()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("주문 내역이 없습니다.");
            }

            return ResponseEntity.ok(orders);
        } catch (Exception e) {
            System.err.println("🔴 주문 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 조회 실패: " + e.getMessage());
        }
    }
    
    
    /** 판매자*/
    // 판매자 주문 조회
    @GetMapping("/seller/orders")
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
    
    // 판매자 주문 상태 변경
    @PatchMapping("/seller/orders/{orderId}")
    public ResponseEntity<?> updateSellerOrderStatus(
    		@RequestParam Long orderId,
			@CookieValue(value = "jwt", required = false) String token) {
    	
        if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
    	return null;
    }
}