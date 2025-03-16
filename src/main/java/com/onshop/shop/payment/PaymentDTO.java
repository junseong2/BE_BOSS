package com.onshop.shop.payment;

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
public class PaymentDTO {

    private String orderId;
    private int amount;
    private String orderName;
    private String userEmail;
    private String paymentMethod;

    // Entity -> DTO 변환
    public static PaymentDTO fromEntity(Payment payment) {
        return PaymentDTO.builder()
                .orderId(payment.getOrder().getOrderId().toString()) // 🔹 orderId는 String으로 변환
                .amount(payment.getTotalAmount())
                .orderName(payment.getOrder().getOrderId().toString()) // 🔹 orderId를 사용하여 설정
                .userEmail(payment.getUser().getEmail()) // 🔹 userId 대신 email 사용
                .paymentMethod(payment.getPaymentMethod())
                .build();
    }
}
