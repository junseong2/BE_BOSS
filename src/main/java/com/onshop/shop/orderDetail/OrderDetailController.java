package com.onshop.shop.orderDetail;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.exception.NotAuthException;
import com.onshop.shop.security.JwtUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/orderdetail")
@RequiredArgsConstructor
@Slf4j
public class OrderDetailController {
	
	private final OrderDetailService orderDetailService;
	private final JwtUtil jwtUtil;
	
	
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDetailResponseDTO> getDetailByOrderId(@PathVariable Long orderId) {
	    return ResponseEntity.ok(orderDetailService.getDetailByOrderId(orderId)); 
	}
	
	@GetMapping("/seller/orders/{orderId}")
	public ResponseEntity<SellerOrderDetailResponseDTO> getOrderDetailsByOrderId(
			@PathVariable Long orderId,
		@CookieValue(value = "jwt", required = false) String token) {
    	
        if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
		log.info("orderId:{}", orderId);
		
		SellerOrderDetailResponseDTO orderDetail  =  orderDetailService.getOrderDetailByOrderId(orderId, userId);
		
		return ResponseEntity.ok(orderDetail);
		
	}


}
