package com.onshop.shop.payment;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/toss")
    public ResponseEntity<?> processTossPayment(@RequestBody PaymentDTO paymentRequest) {
        System.out.println("📌 [PaymentController] 결제 요청 도착");
        System.out.println("📌 요청 데이터: " + paymentRequest);

        // 🔥 필수 데이터 검증
        if (paymentRequest.getUserEmail() == null) {
            System.err.println("❌ [PaymentController] 필수 데이터 누락: " + paymentRequest);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("필수 정보가 누락되었습니다.");
        }

        return paymentService.processTossPayment(paymentRequest);
    }


//    @PostMapping("/kakao")
//    public ResponseEntity<?> processKakaoPayment(@RequestBody PaymentDTO paymentRequest) { // DTO 이름 변경
//        return paymentService.processKakaoPayment(paymentRequest);
//    }
}
 