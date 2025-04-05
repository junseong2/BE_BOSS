package com.onshop.shop.order;

import com.onshop.shop.payment.PaymentStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderStatusResponseDTO {
	private Long orderId;
	private OrderStatus orderStatus;
}
