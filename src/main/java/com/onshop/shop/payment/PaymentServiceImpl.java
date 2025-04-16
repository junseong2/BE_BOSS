package com.onshop.shop.payment;

import java.time.LocalDateTime;



import java.util.HashMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.onshop.shop.exception.NotAuthException;
import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.order.Order;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.onshop.shop.order.OrderRepository;
import com.onshop.shop.seller.Seller;
import com.onshop.shop.seller.SellerRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

    
    /** 판매자*/
	// 판매자 매출 통계
	@Override
	public SellerPaymentStatisticsDTO getSellerPaymentStatistics(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
			
    	Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()->
		new NotAuthException("판매자만 이용 가능합니다.")
		);
	    Long sellerId = seller.getSellerId();
	    
		SellerPaymentStatisticsDTO paymentStatisticsDTO   = paymentRepository.findOrderStatsBySellerId(sellerId, startDate, endDate);

		if(paymentStatisticsDTO == null) {
			throw new ResourceNotFoundException("결제 내역 통계를 찾을 수 없습니다.");
		}
		
		return paymentStatisticsDTO ;
	}

	
	// 판매자 월별 매출 
	@Override
	public Map<String, Long> getSellerPaymentsByMonth(LocalDateTime startDate, LocalDateTime endDate, Long userId) {
    	Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()->
		new NotAuthException("판매자만 이용 가능합니다.")
		);
	    Long sellerId = seller.getSellerId();
	    
		List<SellerPaymentsDTO> monthlySales = paymentRepository.getMonthlySalesBySellerOnlyPaid(sellerId, startDate, endDate);
		
		if(monthlySales.isEmpty()) {
			throw new ResourceNotFoundException("조회할 매출 내역이 없습니다.");
		}
		
		Map<String, Long> salesMap = new HashMap<>();
		
		
		// TODO: 현 방식은 데이터의 크기가 커질수록 비효율적이므로 디비 쿼리에서 처음부터 최적화된 결과를 반환하도록 개선할 필요가 있음을 알아둘 것.
		monthlySales.stream().forEach((sales)->{
			String date = sales.getDate();
			
			if(salesMap.containsKey(date)) {
				Long prevTotal = salesMap.get(date);
				salesMap.put(date, prevTotal + sales.getTotalAmount());
			} else {
				salesMap.put(date, sales.getTotalAmount());
			}
		});
		
		return salesMap;
		
	}

	// 판매자 월별 카테고리별 매출 통계 비율
	@Override
	public List<SellerCategorySalesDTO> getSellerPaymentSalesByCategory(LocalDateTime startDate,LocalDateTime endDate, Long userId) {
		
    	Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()->
		new NotAuthException("판매자만 이용 가능합니다.")
		);
	    Long sellerId = seller.getSellerId();
	    
		List<SellerCategorySalesDTO> categorySalesDTOs = paymentRepository.getCategorySalesBySeller(sellerId, startDate, endDate);
		
		if(categorySalesDTOs.isEmpty()) {
			throw new ResourceNotFoundException("조회할 매출 내역이 없습니다.");
		}
		return categorySalesDTOs;
	}
}
