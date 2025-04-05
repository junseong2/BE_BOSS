package com.onshop.shop.order;

import java.time.LocalDateTime;

import com.onshop.shop.user.UserResponseDTO;

import lombok.Data;

@Data
public class OrderResponseDTO {
    private Long orderId;
    private int totalPrice;
    private String status;
    private LocalDateTime createdDate;
    private UserResponseDTO user;
    // 필요에 따라 Payment 정보나 기타 필드를 추가

    // Getter, Setter
}
