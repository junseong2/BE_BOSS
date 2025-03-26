package com.onshop.shop.order;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onshop.shop.inventory.InventoryService;
import com.onshop.shop.orderDetail.OrderDetailService;
import com.onshop.shop.security.JwtUtil;
import com.onshop.shop.user.User;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("")
public class OrderController {

    private final OrderService orderService;
    private final OrderDetailService orderDetailService;
    private final JwtUtil jwtUtil;

    
    
    
    @PostMapping("/orders/create")
    public ResponseEntity<?> createOrder(
    		@RequestBody OrderDTO orderDTO, 
    		@CookieValue(value = "jwt", required = false) String token) {
     
    	Long userId = jwtUtil.extractUserId(token); // ✅ JWT에서 userId 추출

    	try {
            System.out.println("📩 [DEBUG] 주문 생성 요청: " + orderDTO);
            System.out.println("🔹 userId: " + orderDTO.getUserId());
            System.out.println("🔹 totalPrice: " + orderDTO.getTotalPrice());

            if (orderDTO.getUserId() == null) {
                throw new IllegalArgumentException("❌ userId가 null입니다!");
            }

            Order order = orderService.createOrder(orderDTO);
            orderDetailService.createOrderDetail(userId, orderDTO, order);
            

            System.out.println("✅ 주문 생성 완료! Order ID: " + order.getOrderId());

            return ResponseEntity.ok(Map.of("orderId", order.getOrderId()));
        } catch (Exception e) {
            System.err.println("🔴 주문 생성 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 생성 실패: " + e.getMessage());
        }
    }

    // ✅ 주문 조회 API
    @GetMapping("/orders/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId ) {
        Order order = orderService.getOrderById(orderId);
        return ResponseEntity.ok(order);
    }
    
    /** 판매자*/
    // 판매자 주문 조회
    @GetMapping("/seller/orders")
    public ResponseEntity<?> getSellerOrders(
    		@RequestParam int page,
    		@RequestParam int size,
    		@RequestParam String search,
    		@RequestParam String status
    		){
    	
    	SellerOrderResponseDTO orders = orderService.getOrders(page, size, search, status);
    	
    	return ResponseEntity.ok(orders);
    } 
    
    // 판매자 주문 상태 변경
    
    
    
}
