package com.onshop.shop.payment;

import java.util.Optional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PaymentService {
    Payment createPayment(PaymentDTO paymentDTO);
    Optional<Payment> getPaymentByImpUid(String impUid);
    void updatePaymentStatus(String impUid, PaymentStatus status);
    String getAccessToken();
    Map<String, Object> getPaymentInfo(String impUid);
    
    
    /** 판매자 */
    // 판매자 매출 통계
    SellerPaymentStatisticsDTO getSellerPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate);
    
    // 판매자 월별 매출 합계
    Map<String, Long> getSellerPaymentsByMonth(LocalDateTime startDate, LocalDateTime endDate);
    
}
