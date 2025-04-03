package com.onshop.shop.orderDetail;

import java.time.LocalDateTime;

import com.onshop.shop.payment.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponseDTO {
	  	private Long orderId;            // 주문 번호
	    private LocalDateTime createdDate;  // 주문 일시
	    private Long totalQuantity;       // 주문 상품 총 개수
	    private int totalPayment; // 결제 금액 (부가세 및 배송비 포함)
	    private Object paidDate;  // 결제 일시
	    private PaymentMethod paymentMethod; // 결제 방법
	    private String username;         // 받는 사람
	    private String phoneNumber;      // 연락처
	    private String address1;         // 배송 주소 1
	    private String address2;         // 배송 주소 2
	    private String post;             // 우편번호
	    private String productName;      // 주문 상품
}
