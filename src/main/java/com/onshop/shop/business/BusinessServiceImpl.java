package com.onshop.shop.business;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    private final ObjectMapper objectMapper;
     
    @Value("${business.secret-key}")
    private String secretKey;
    
    @Override
    public Map<String, Object> updateCompanyStatus(String bsnsLcns) {
        Map<String, Object> result = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // URL을 URI 객체로 변환
            String url = "https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=" + secretKey;
            URI uri = new URI(url);  // String url을 URI로 변환
            System.out.println("🔍 요청 URI: " + uri);

            // 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

            // 요청 본문 준비
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("b_no", Collections.singletonList(bsnsLcns));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            System.out.println("📢 요청 본문: " + objectMapper.writeValueAsString(requestBody));

            // 헤더 설정

            // 요청 보내기
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Map.class);
            
            System.out.println("✅ 응답 상태: " + response.getStatusCode());
            System.out.println("📄 응답 본문: " + objectMapper.writeValueAsString(response.getBody()));


            // 응답 확인
            result = response.getBody();
            return result;
        } catch (Exception e) {
            // 오류 처리
            e.printStackTrace();
            result.put("error", "API 호출 오류 발생: " + e.getMessage());
            return result;
        }
    }
}