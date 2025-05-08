package com.onshop.shop.domain.business.service;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사업자 등록번호를 통해 사업자 상태를 조회하는 서비스 구현체입니다.
 * 
 * <p>
 * 외부 공공 데이터 포털 API(https://www.data.go.kr) 를 호출하여
 * 사업자 등록 상태를 확인하고 그 결과를 반환합니다.
 * </p>
 * 
 * <p>
 * API 호출 시 필요한 Secret Key는 application 설정 파일에서 주입됩니다.
 * </p>
 * 
 * <p>
 * 반환 결과는 원시 Map 형태이며, 성공 시 사업자 상태 정보가 포함되고,
 * 실패 시 오류 메시지를 포함합니다.
 * </p>
 * 
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BusinessServiceImpl implements BusinessService {

    /** JSON 직렬화 및 역직렬화를 위한 ObjectMapper */
    private final ObjectMapper objectMapper;

    /** 외부 API 인증을 위한 시크릿 키 (application.yml 또는 properties에서 주입) */
    @Value("${business.secret-key}")
    private String secretKey;

    /**
     * 외부 API를 호출하여 해당 사업자 등록번호의 상태를 조회합니다.
     * 
     * <p>
     * 요청 본문은 다음과 같은 JSON 구조로 구성됩니다:
     * <pre>{@code
     * {
     *   "b_no": ["1234567890"]
     * }
     * }</pre>
     * </p>
     * 
     * <p>
     * 요청 성공 시 응답 JSON을 Map으로 반환합니다.
     * 요청 실패 시 에러 메시지가 포함된 Map을 반환합니다.
     * </p>
     * 
     * @param bsnsLcns 사업자 등록번호 (예: "1234567890")
     * @return 외부 API 응답 결과를 담은 Map
     */
    @Override
    public Map<String, Object> updateCompanyStatus(String bsnsLcns) {
        Map<String, Object> result = new HashMap<>();
        try {
            RestTemplate restTemplate = new RestTemplate();
            
            // 외부 API URI 구성
            String url = "https://api.odcloud.kr/api/nts-businessman/v1/status?serviceKey=" + secretKey;
            URI uri = new URI(url);  // 문자열 URL을 URI 객체로 변환
            System.out.println("🔍 요청 URI: " + uri);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON)); // 사업자 번호 한 개만 보낼 때도, API가 리스트 형식으로 받으므로 싱글톤 리스트를 적용함

            // 요청 바디 구성
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("b_no", Collections.singletonList(bsnsLcns));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
            System.out.println("📢 요청 본문: " + objectMapper.writeValueAsString(requestBody));

            // API 호출
            ResponseEntity<Map> response = restTemplate.exchange(uri, HttpMethod.POST, entity, Map.class);

            // 응답 로깅
            log.info("✅ 응답 상태: {}", response.getStatusCode());
            log.info("📄 응답 본문: {}", objectMapper.writeValueAsString(response.getBody()));

            // 결과 반환
            result = response.getBody();
            return result;
        } catch (Exception e) {
            // 예외 발생 시 오류 메시지 반환
            e.printStackTrace();
            result.put("error", "API 호출 오류 발생: " + e.getMessage());
            return result;
        }
    }
}
