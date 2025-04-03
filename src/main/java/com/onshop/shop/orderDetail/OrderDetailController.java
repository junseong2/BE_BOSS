package com.onshop.shop.orderDetail;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@RestController
@RequestMapping("/orderdetail")
@RequiredArgsConstructor
@Slf4j
public class OrderDetailController {
	
	private final OrderDetailService orderDetailService;
	
	
	@GetMapping("/{orderId}")
	public ResponseEntity<OrderDetailResponseDTO> getDetailByOrderId(@PathVariable Long orderId) {
	    return ResponseEntity.ok(orderDetailService.getDetailByOrderId(orderId)); 
	}


}
