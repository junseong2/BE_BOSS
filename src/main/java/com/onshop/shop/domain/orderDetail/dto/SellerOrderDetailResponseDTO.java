package com.onshop.shop.domain.orderDetail.dto;

import java.time.LocalDateTime;

import com.onshop.shop.domain.payment.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SellerOrderDetailResponseDTO {
    private Long orderId; // 주문 번호
    private LocalDateTime createdDate; // 주문 일시
    private Object totalQuantity; // 주문 상품 총 개수
    private Object totalPayment; // 결제 금액 (부가세 및 배송비 포함)
    private Object paidDate; // 결제 일시
    private PaymentMethod paymentMethod; // 결제 방법
    private Object username; // 받는 사람
    private Object phoneNumber; // 연락처
    private Object address; // 배송 주소
    private Object productName;

}