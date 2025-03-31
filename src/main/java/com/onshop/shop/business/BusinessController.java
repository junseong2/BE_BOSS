package com.onshop.shop.business;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.util.Map;

@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping("/check")
    public ResponseEntity<?> checkBusiness(@RequestBody BusinessDTO request) {
        if (request.getBusinesses() == null || request.getBusinesses().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("message", "사업자등록번호 목록을 제공해주세요"));
        }

        try {
            // 사업자 등록번호로 상태 업데이트
            Map<String, Object> result = businessService.updateCompanyStatus(request.getBusinesses().get(0).getB_no());
            return ResponseEntity.ok(result);
        
        } catch (Exception e) {
            // 일반적인 예외 처리
            return ResponseEntity.status(500).body(Map.of("error", "서버 오류 발생: " + e.getMessage()));
        }
    }
}
