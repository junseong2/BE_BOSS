package com.onshop.shop.payment;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class PaymentDTO {
    @NotNull(message = "사용자 ID는 필수입니다.")
    private Long userId;

    @NotNull(message = "주문 ID는 필수입니다.")
    private Long orderId;

    @NotNull(message = "결제 금액은 필수입니다.")
    private BigDecimal totalAmount;

    @NotNull(message = "결제 방법을 선택해야 합니다.")
    private PaymentMethod paymentMethod;
    
    private String impUid;
}
