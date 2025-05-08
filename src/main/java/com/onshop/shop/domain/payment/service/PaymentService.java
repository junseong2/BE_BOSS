package com.onshop.shop.domain.payment.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.onshop.shop.domain.payment.dto.PaymentDTO;
import com.onshop.shop.domain.payment.dto.SellerCategorySalesDTO;
import com.onshop.shop.domain.payment.dto.SellerPaymentStatisticsDTO;
import com.onshop.shop.domain.payment.entity.Payment;
import com.onshop.shop.domain.payment.enums.PaymentStatus;

public interface PaymentService {
    Payment createPayment(PaymentDTO paymentDTO);
    Optional<Payment> getPaymentByImpUid(String impUid);
    void updatePaymentStatus(String impUid, PaymentStatus status);
    String getAccessToken();
    Map<String, Object> getPaymentInfo(String impUid);
    
    
    /** 판매자 */
    // 판매자 매출 통계
    SellerPaymentStatisticsDTO getSellerPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate, Long userId);
    
    // 판매자 월별 매출 합계
    Map<String, Long> getSellerPaymentsByMonth(LocalDateTime startDate, LocalDateTime endDate, Long userId);
    
    // 판매자별 카테고리별 매출 비율 통계
    List<SellerCategorySalesDTO> getSellerPaymentSalesByCategory(LocalDateTime startDate,LocalDateTime endDate, Long userId);

}