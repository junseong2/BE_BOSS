package com.onshop.shop.user;

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

@Service
public class FaceRecognitionService {

    @Value("${facepp.api.key}")
    private String faceppApiKey;

    @Value("${facepp.api.secret}")
    private String faceppApiSecret;

    private final RestTemplate restTemplate;

    private static final String DETECT_URL = "https://api-us.faceplusplus.com/facepp/v3/detect";
    private static final String SEARCH_URL = "https://api-us.faceplusplus.com/facepp/v3/search";
    private static final String ADD_FACESET_URL = "https://api-us.faceplusplus.com/facepp/v3/faceset/addface";

    public FaceRecognitionService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String detectFace(String imageData) {
        // 얼굴 감지 API 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> params = new HashMap<>();
        params.put("api_key", faceppApiKey);
        params.put("api_secret", faceppApiSecret);
        params.put("image_base64", imageData);
        params.put("return_attributes", "gender,age,emotion");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(DETECT_URL, HttpMethod.POST, request, String.class);

        return response.getBody();
    }

    public String searchFace(String imageData) {
        // 얼굴 검색 API 호출
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> params = new HashMap<>();
        params.put("api_key", faceppApiKey);
        params.put("api_secret", faceppApiSecret);
        params.put("image_base64", imageData);
        params.put("faceset_token", "your_faceset_token");

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(SEARCH_URL, HttpMethod.POST, request, String.class);

        return response.getBody();
    }


    public String addFace(String imageData, String userId) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        Map<String, String> params = new HashMap<>();
        params.put("api_key", faceppApiKey);
        params.put("api_secret", faceppApiSecret);
        params.put("image_base64", imageData);
        params.put("outer_id", "your_faceset_token");
        params.put("user_id", userId);

        HttpEntity<Map<String, String>> request = new HttpEntity<>(params, headers);
        ResponseEntity<String> response = restTemplate.exchange(ADD_FACESET_URL, HttpMethod.POST, request, String.class);

        return response.getBody();
    }

}
