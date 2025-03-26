package com.onshop.shop.payment;

import java.util.Optional;
import java.util.List;
import java.util.Map;

public interface PaymentService {
    Payment createPayment(PaymentDTO paymentDTO);
    Optional<Payment> getPaymentByImpUid(String impUid);
    void updatePaymentStatus(String impUid, PaymentStatus status);
    String getAccessToken();
    Map<String, Object> getPaymentInfo(String impUid);
    
    
    /** 판매자 */
    // 판매자 결제 내역 조회
    SellerPaymentResponseDTO getSellerPayments(int page, int size, String search, String status);
    
}
