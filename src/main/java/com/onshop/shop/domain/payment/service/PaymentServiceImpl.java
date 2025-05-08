package com.onshop.shop.domain.payment.service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.onshop.shop.domain.order.entity.Order;
import com.onshop.shop.domain.order.repository.OrderRepository;
import com.onshop.shop.domain.payment.dto.PaymentDTO;
import com.onshop.shop.domain.payment.dto.SellerCategorySalesDTO;
import com.onshop.shop.domain.payment.dto.SellerPaymentStatisticsDTO;
import com.onshop.shop.domain.payment.dto.SellerPaymentsDTO;
import com.onshop.shop.domain.payment.entity.Payment;
import com.onshop.shop.domain.payment.enums.PaymentStatus;
import com.onshop.shop.domain.payment.repository.PaymentRepository;
import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.global.exception.NotAuthException;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 서비스 구현 클래스입니다. 이 클래스는 결제 관련 비즈니스 로직을 처리합니다.
 * 주요 기능으로는 결제 생성, 상태 업데이트, 판매자 매출 통계 조회 등이 포함됩니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final RestTemplate restTemplate;
    private final SellerRepository sellerRepository;

    @Value("${portone.api-key}")
    private String apiKey;

    @Value("${portone.secret-key}")
    private String secretKey;

    /**
     * 결제를 생성합니다.
     * 주문 ID와 결제 정보를 받아 새로운 결제 엔티티를 생성하고 저장합니다.
     * 
     * @param paymentDTO 결제 정보 DTO
     * @return 생성된 결제 엔티티
     * @throws IllegalArgumentException 주문이 존재하지 않을 경우
     */
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

    /**
     * 결제 상태를 업데이트합니다.
     * 주어진 impUid에 해당하는 결제 상태를 지정된 상태로 변경합니다.
     * 
     * @param impUid 결제 고유 ID
     * @param status 결제 상태
     */
    @Override
    @Transactional
    public void updatePaymentStatus(String impUid, PaymentStatus status) {
        paymentRepository.findByImpUid(impUid).ifPresent(payment -> {
            payment.setPaymentStatus(status);
            payment.setPaidDate(LocalDateTime.now());
            paymentRepository.save(payment);
        });
    }

    /**
     * impUid를 기준으로 결제 정보를 조회합니다.
     * 
     * @param impUid 결제 고유 ID
     * @return 결제 정보가 존재하는 경우 해당 결제 엔티티, 없으면 {@link Optional#empty()}
     */
    @Override
    public Optional<Payment> getPaymentByImpUid(String impUid) {
        return paymentRepository.findByImpUid(impUid);
    }

    /**
     * 결제 API의 액세스 토큰을 가져옵니다.
     * 
     * @return 액세스 토큰
     */
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

    /**
     * 결제 정보를 조회합니다.
     * 
     * @param impUid 결제 고유 ID
     * @return 결제 정보
     */
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

    /** 판매자 관련 메서드 **/

    /**
     * 판매자의 결제 통계를 조회합니다.
     * 
     * @param startDate 통계 시작 날짜
     * @param endDate 통계 종료 날짜
     * @param userId 요청한 사용자의 ID
     * @return 판매자 결제 통계 {@link SellerPaymentStatisticsDTO}
     * @throws NotAuthException 사용자에게 판매자 권한이 없는 경우
     * @throws ResourceNotFoundException 결제 내역을 찾을 수 없는 경우
     */
    @Override
    public SellerPaymentStatisticsDTO getSellerPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
        Seller seller = sellerRepository.findByUserId(userId).orElseThrow(() ->
            new NotAuthException("판매자만 이용 가능합니다.")
        );
        Long sellerId = seller.getSellerId();
        SellerPaymentStatisticsDTO paymentStatisticsDTO = paymentRepository.findOrderStatsBySellerId(sellerId, startDate, endDate);

        if (paymentStatisticsDTO == null) {
            throw new ResourceNotFoundException("결제 내역 통계를 찾을 수 없습니다.");
        }

        return paymentStatisticsDTO;
    }

    /**
     * 판매자의 월별 매출을 조회합니다.
     * 
     * @param startDate 통계 시작 날짜
     * @param endDate 통계 종료 날짜
     * @param userId 요청한 사용자의 ID
     * @return 월별 매출 {@link Map<String, Long>}
     * @throws NotAuthException 사용자에게 판매자 권한이 없는 경우
     * @throws ResourceNotFoundException 조회된 매출 내역이 없는 경우
     */
    @Override
    public Map<String, Long> getSellerPaymentsByMonth(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
        Seller seller = sellerRepository.findByUserId(userId).orElseThrow(() ->
            new NotAuthException("판매자만 이용 가능합니다.")
        );
        Long sellerId = seller.getSellerId();
        List<SellerPaymentsDTO> monthlySales = paymentRepository.getMonthlySalesBySellerOnlyPaid(sellerId, startDate, endDate);

        if (monthlySales.isEmpty()) {
            throw new ResourceNotFoundException("조회할 매출 내역이 없습니다.");
        }

        Map<String, Long> salesMap = new HashMap<>();

        monthlySales.stream().forEach((sales) -> {
            String date = sales.getDate();

            if (salesMap.containsKey(date)) {
                Long prevTotal = salesMap.get(date);
                salesMap.put(date, prevTotal + sales.getTotalAmount());
            } else {
                salesMap.put(date, sales.getTotalAmount());
            }
        });

        return salesMap;
    }

    /**
     * 판매자의 카테고리별 매출 통계를 조회합니다.
     * 
     * @param startDate 통계 시작 날짜
     * @param endDate 통계 종료 날짜
     * @param userId 요청한 사용자의 ID
     * @return 카테고리별 매출 통계 {@link List<SellerCategorySalesDTO>}
     * @throws NotAuthException 사용자에게 판매자 권한이 없는 경우
     * @throws ResourceNotFoundException 조회된 매출 내역이 없는 경우
     */
    @Override
    public List<SellerCategorySalesDTO> getSellerPaymentSalesByCategory(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
        Seller seller = sellerRepository.findByUserId(userId).orElseThrow(() ->
            new NotAuthException("판매자만 이용 가능합니다.")
        );
        Long sellerId = seller.getSellerId();
        List<SellerCategorySalesDTO> categorySalesDTOs = paymentRepository.getCategorySalesBySeller(sellerId, startDate, endDate);

        if (categorySalesDTOs.isEmpty()) {
            throw new ResourceNotFoundException("조회할 매출 내역이 없습니다.");
        }

        return categorySalesDTOs;
    }
}
