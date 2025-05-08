package com.onshop.shop.domain.orderDetail.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.onshop.shop.domain.payment.enums.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDetailResponseDTO {
    private Long orderId; // 주문 번호
    private LocalDateTime createdDate; // 주문 일시
    private Object totalQuantity; // 주문 상품 총 개수
    private Object totalPayment; // 결제 금액 (부가세 및 배송비 포함)
    private Object paidDate; // 결제 일시
    private PaymentMethod paymentMethod; // 결제 방법
    private Object username; // 받는 사람
    private Object phoneNumber; // 연락처
    private Object address; // 배송 주소
    private List<ProductItemDTO> products;
    
    public OrderDetailResponseDTO(Long orderId, LocalDateTime createdDate, Object totalQuantity, Object totalPayment,
    	    Object paidDate, PaymentMethod paymentMethod, Object username, Object phoneNumber, Object address) {
    	    this.orderId = orderId;
    	    this.createdDate = createdDate;
    	    this.totalQuantity = totalQuantity;
    	    this.totalPayment = totalPayment;
    	    this.paidDate = paidDate;
    	    this.paymentMethod = paymentMethod;
    	    this.username = username;
    	    this.phoneNumber = phoneNumber;
    	    this.address = address;
    	    this.products = new ArrayList<ProductItemDTO>();
    	}

}