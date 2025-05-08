package com.onshop.shop.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderDTO {
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "총 금액은 필수입니다.")
    private int totalPrice;
}
