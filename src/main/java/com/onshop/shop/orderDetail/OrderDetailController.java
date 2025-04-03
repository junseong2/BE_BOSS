package com.onshop.shop.orderDetail;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;


@RestController
@RequestMapping
@RequiredArgsConstructor
public class OrderDetailController {
	
	private final OrderDetailService orderDetailService;
	
	
	@GetMapping("/seller/orders/{orderid}")
	public ResponseEntity<OrderDetailResponseDTO> getOrderDetailsByOrderId(
			@Valid @PathVariable Long orderId
			){
		
		OrderDetailResponseDTO orderDetails=  orderDetailService.getOrderDetailByOrderId(orderId);
		
		return ResponseEntity.ok(orderDetails);
		
	}

}
