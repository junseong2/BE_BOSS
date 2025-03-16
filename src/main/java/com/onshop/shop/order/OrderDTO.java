package com.onshop.shop.order;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {
    private Long orderId;
    private Integer userId; // 🔹 Long → Integer 변경
    private int totalPrice;
    private String status;
    private LocalDateTime createdDate;

    public static OrderDTO fromEntity(Order order) {
        return OrderDTO.builder()
                .orderId(order.getOrderId())
                .userId(order.getUser().getUserId()) // 🔹 Integer 타입 유지
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().name())
                .createdDate(order.getCreatedDate())
                .build();
    }
}
