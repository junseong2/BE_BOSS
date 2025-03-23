package com.onshop.shop.business;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import java.util.*;

@Service
public class BusinessServiceImpl implements BusinessService {

    private static final String API_URL = "https://api.odcloud.kr/api/nts-businessman/v1/status";
    private static final String SERVICE_KEY = "y1FW4%2F3TnwGJ8u7UCS%2BnPtTX61ihPg9JDn03WDG898KAC0GNXPYeUVeUOXLorvfwLrcS31gXgz2I1GEz5uJicg%3D%3D"; // 🔹 공공데이터포털에서 받은 API 키 입력

    @Override
    public String checkBusinessStatus(String businessNumber) {
        RestTemplate restTemplate = new RestTemplate();

        // 요청 데이터 (JSON 형식)
        Map<String, List<Map<String, String>>> requestBody = new HashMap<>();
        List<Map<String, String>> businesses = new ArrayList<>();
        Map<String, String> business = new HashMap<>();
        business.put("b_no", businessNumber);
        businesses.add(business);
        requestBody.put("businesses", businesses);

        // HTTP 요청 헤더 설정
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + SERVICE_KEY);

        // HTTP 요청 생성
        HttpEntity<Map<String, List<Map<String, String>>>> requestEntity = new HttpEntity<>(requestBody, headers);

        // API 요청 보내기
        ResponseEntity<String> response = restTemplate.exchange(API_URL, HttpMethod.POST, requestEntity, String.class);

        return response.getBody();
    }
}
