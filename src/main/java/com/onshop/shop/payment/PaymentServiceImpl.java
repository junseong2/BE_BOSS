package com.onshop.shop.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import com.onshop.shop.order.Order;
import com.onshop.shop.order.OrderRepository;
import java.util.Optional;
import java.util.Map;
import java.time.LocalDateTime;
import java.util.HashMap;

@Service
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.secret-key}")
    private String secretKey;

    public PaymentServiceImpl(PaymentRepository paymentRepository, OrderRepository orderRepository, RestTemplate restTemplate) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.restTemplate = restTemplate;
    }

    @Override
    @Transactional
    public Payment createPayment(PaymentDTO paymentDTO) {
        Order order = orderRepository.findById(paymentDTO.getOrderId())
            .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. orderId: " + paymentDTO.getOrderId()));

        Payment payment = new Payment();
        payment.setUserId(paymentDTO.getUserId());
        payment.setOrder(order);
        payment.setTotalAmount(paymentDTO.getTotalAmount());
        payment.setPaymentMethod(paymentDTO.getPaymentMethod());
        payment.setPaymentStatus(PaymentStatus.PENDING);
        payment.setImpUid(paymentDTO.getImpUid());

        return paymentRepository.save(payment);
    }

    @Override
    @Transactional
    public void updatePaymentStatus(String impUid, PaymentStatus status) {
        paymentRepository.findByImpUid(impUid).ifPresent(payment -> {
            payment.setPaymentStatus(status);
            payment.setPaidDate(LocalDateTime.now());
            paymentRepository.save(payment);
        });
    }

    @Override
    public Optional<Payment> getPaymentByImpUid(String impUid) {
        return paymentRepository.findByImpUid(impUid);
    }

    @Override
    public String getAccessToken() {
        String url = "https://api.iamport.kr/users/getToken";
        Map<String, String> requestBody = Map.of("imp_key", apiKey, "imp_secret", secretKey);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
        return ((Map<String, Object>) response.getBody().get("response")).get("access_token").toString();
    }

    @Override
    public Map<String, Object> getPaymentInfo(String impUid) {
        String accessToken = getAccessToken();
        String url = "https://api.iamport.kr/payments/" + impUid;
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + accessToken);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);
        return response.getBody();
    }


}
