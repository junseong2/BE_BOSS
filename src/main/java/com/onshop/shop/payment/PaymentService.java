package com.onshop.shop.payment;
import org.springframework.http.ResponseEntity;

public interface PaymentService {
    ResponseEntity<?> processTossPayment(PaymentDTO paymentRequest);
//    ResponseEntity<?> processKakaoPayment(PaymentDTO paymentRequest);
}
