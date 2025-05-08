package com.onshop.shop.domain.order.dto;

import com.onshop.shop.domain.order.enums.OrderStatus;

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
