package com.onshop.shop.payment;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.onshop.shop.order.Order;
import com.onshop.shop.order.OrderRepository;
import com.onshop.shop.order.OrderStatus;
import com.onshop.shop.user.User;
import com.onshop.shop.user.UserRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${payment.toss.api-url}")
    private String tossApiUrl;

    @Value("${payment.toss.secret-key}")
    private String tossSecretKey;

//    @Value("${payment.kakao.api-url}")
//    private String kakaoApiUrl;
//
//    @Value("${payment.kakao.admin-key}")
//    private String kakaoAdminKey;
    
    @PostConstruct
    public void checkConfig() {
        System.out.println("💡 Toss API URL: " + tossApiUrl);
        System.out.println("💡 Toss Secret Key: " + tossSecretKey);
    }

    @Override
    public ResponseEntity<?> processTossPayment(PaymentDTO request) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBasicAuth(tossSecretKey, "");

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("amount", request.getAmount());
            requestBody.put("orderId", request.getOrderId());
            requestBody.put("orderName", request.getOrderName());
            requestBody.put("customerEmail", request.getUserEmail());
            requestBody.put("successUrl", "http://localhost:5173/payment/success");
            requestBody.put("failUrl", "http://localhost:5173/payment/fail");

            HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(requestBody), headers);
            ResponseEntity<String> response = restTemplate.exchange(tossApiUrl, HttpMethod.POST, entity, String.class);

            savePayment(request, "SUCCESS");
            return ResponseEntity.ok(response.getBody());

        } catch (Exception e) {
            savePayment(request, "FAIL");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("토스 결제 실패");
        }
    }

//    @Override
//    public ResponseEntity<?> processKakaoPayment(PaymentDTO request) {
//        try {
//            HttpHeaders headers = new HttpHeaders();
//            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//            headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
//
//            MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();
//            requestBody.add("cid", "TC0ONETIME");
//            requestBody.add("partner_order_id", request.getOrderId());
//            requestBody.add("partner_user_id", request.getUserEmail());
//            requestBody.add("item_name", request.getOrderName());
//            requestBody.add("quantity", "1");
//            requestBody.add("total_amount", String.valueOf(request.getAmount()));
//            requestBody.add("tax_free_amount", "0");
//            requestBody.add("approval_url", "http://localhost:5173/payment/success");
//            requestBody.add("cancel_url", "http://localhost:5173/payment/cancel");
//            requestBody.add("fail_url", "http://localhost:5173/payment/fail");
//
//            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(requestBody, headers);
//            ResponseEntity<String> response = restTemplate.exchange(kakaoApiUrl, HttpMethod.POST, entity, String.class);
//
//            savePayment(request, "SUCCESS");
//            return ResponseEntity.ok(response.getBody());
//
//        } catch (Exception e) {
//            savePayment(request, "FAIL");
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("카카오 결제 실패");
//        }
//    }

    private void savePayment(PaymentDTO request, String status) {
        if (request.getUserEmail() == null || request.getUserEmail().isEmpty() || request.getUserEmail().equals("이메일 없음")) {
            throw new IllegalArgumentException("🚨 유효한 이메일이 필요합니다! (현재 값: " + request.getUserEmail() + ")");
        }

        User user = userRepository.findByEmail(request.getUserEmail())
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. (이메일: " + request.getUserEmail() + ")"));

        Order order;

        if (request.getOrderId() != null && !request.getOrderId().isEmpty()) {
            // 기존 주문 찾기
            order = orderRepository.findById(Long.parseLong(request.getOrderId()))
                    .orElseThrow(() -> new RuntimeException("주문을 찾을 수 없습니다."));
        } else {
            // 새로운 주문 생성
            order = Order.builder()
                    .user(user)
                    .totalPrice(request.getAmount())
                    .status(OrderStatus.PENDING)
                    .createdDate(LocalDateTime.now())
                    .build();

            orderRepository.save(order);
        }

        // 결제 정보 저장
        Payment payment = Payment.builder()
                .order(order)
                .user(user)
                .totalAmount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                .paymentStatus(status)
                .paidDate(LocalDateTime.now())
                .build();

        paymentRepository.save(payment);
    }

}
