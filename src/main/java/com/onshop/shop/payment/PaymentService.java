package com.onshop.shop.payment;

import java.util.Optional;
import java.util.Map;

public interface PaymentService {
    Payment createPayment(PaymentDTO paymentDTO);
    Optional<Payment> getPaymentByImpUid(String impUid);
    void updatePaymentStatus(String impUid, PaymentStatus status);
    String getAccessToken();
    Map<String, Object> getPaymentInfo(String impUid);
}
