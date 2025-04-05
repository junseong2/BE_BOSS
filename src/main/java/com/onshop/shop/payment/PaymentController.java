package com.onshop.shop.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.onshop.shop.order.Order;
import com.onshop.shop.order.OrderDTO;
import com.onshop.shop.order.OrderService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping()
public class PaymentController {

    private final PaymentService paymentService;
    private final OrderService orderService;
    private final PaymentConfig paymentConfig;

    public PaymentController(PaymentService paymentService,OrderService orderService, PaymentConfig paymentConfig) {
        this.paymentService = paymentService;
        this.orderService = orderService;
        this.paymentConfig = paymentConfig;
    }
    
    // ✅ 프론트에서 사용할 채널 키 반환 API 추가
    @GetMapping("/payment/channel-key/{paymentMethod}")
    public Map<String, String> getChannelKey(@PathVariable String paymentMethod) {
        String channelKey = paymentConfig.getChannelKey(paymentMethod);
        return Map.of("channelKey", channelKey);
    }

    @PostMapping("/payment/portone")
    public ResponseEntity<?> createPayment(@RequestBody PaymentDTO paymentDTO) {
        try {
            System.out.println("📩 [DEBUG] 받은 결제 요청 데이터: " + paymentDTO); // ✅ 디버깅 로그 추가

            Payment payment = paymentService.createPayment(paymentDTO);

            System.out.println("📩 [DEBUG] 저장된 결제 ID: " + payment.getPaymentId()); // ✅ 디버깅 로그 추가

            return ResponseEntity.ok(Map.of("message", "결제 요청 성공", "impUid", payment.getImpUid()));
        } catch (Exception e) {
            System.err.println("🔴 결제 처리 중 오류 발생: " + e.getMessage()); // ✅ 에러 로그 출력
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("결제 처리 실패: " + e.getMessage());
        }
    }


    @PostMapping("/payment/update-status")
    public ResponseEntity<?> updatePaymentStatus(@RequestBody Map<String, String> request) {
        try {
            String impUid = request.get("impUid");
            String status = request.get("status");
            PaymentStatus paymentStatus = PaymentStatus.valueOf(status.toUpperCase());
            paymentService.updatePaymentStatus(impUid, paymentStatus);
            return ResponseEntity.ok(Map.of("message", "결제 상태 업데이트 성공"));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("결제 상태 업데이트 실패: " + e.getMessage());
        }
    }
   
    
    /** 판매자 매출관리 통계 */
    @GetMapping("/seller/payments/summary-statistics")
    public ResponseEntity<?> getSellerPaymentstatistics(
    		@RequestParam LocalDate startDate,
    		@RequestParam LocalDate endDate
    		){
    	
    	LocalDateTime startDateTime= startDate.atStartOfDay();
    	LocalDateTime endDateTime =  endDate.atTime(23, 59, 59);
    	SellerPaymentStatisticsDTO paymentStatisticsDTO= paymentService.getSellerPaymentStatistics(startDateTime, endDateTime);
    	
    	return ResponseEntity.ok(paymentStatisticsDTO);
    	
    }
    
    /** 판매자 월별 매출 통계*/
    @GetMapping("/seller/payments/monthly-statistics")
    public ResponseEntity<?> getSellerPaymentsByMonth(
    		@RequestParam LocalDate startDate,
    		@RequestParam LocalDate endDate
    		){
    	
    	Map<String,Long> payments = paymentService.getSellerPaymentsByMonth(startDate.atStartOfDay(), endDate.atTime(23,59,59));
    	return ResponseEntity.ok(payments);
    }
    
    /** 판매자 카테고리별 매출 비율 통계*/
    @GetMapping("/seller/payments/category-statistics")
    public ResponseEntity<?> getSellerPaymentSalesByCategory(
    		@RequestParam LocalDate startDate,
    		@RequestParam LocalDate endDate
    		){
    	
    	List<SellerCategorySalesDTO> categorySalesDTOs = paymentService.getSellerPaymentSalesByCategory(startDate.atStartOfDay(), endDate.atTime(23, 59, 59));
    	return ResponseEntity.ok(categorySalesDTOs);
    }
}
