package com.onshop.shop.domain.business.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.domain.business.dto.BusinessDTO;
import com.onshop.shop.domain.business.service.BusinessService;

import lombok.RequiredArgsConstructor;

/**
 * 사업자 등록번호 유효성을 체크하는 요청을 처리하는 컨트롤러 클래스입니다.
 * 
 * <p>
 * 클라이언트로부터 사업자 등록번호 리스트를 받아 사업자의 상태를 확인하고,
 * 해당 상태 정보를 반환합니다.
 * </p>
 * 
 * <p>
 * 주 기능:
 * <ul>
 *     <li>사업자 등록번호 유효성 검증</li>
 *     <li>외부 API 또는 내부 서비스 호출을 통해 사업자 상태 업데이트</li>
 * </ul>
 * </p>
 * 
 */
@RestController
@RequestMapping("/business")
@RequiredArgsConstructor
public class BusinessController {

    /** 사업자 상태 확인 및 갱신 로직을 담당하는 서비스 */
    private final BusinessService businessService;

    /**
     * 클라이언트로부터 사업자 등록번호을 전달받아 상태를 확인하고 응답합니다.
     * 
     * <p>
     * 예시 요청 형식:
     * <pre>{@code
     * POST /business/check
     * Content-Type: application/json
     * 
     * {
     *   "businesse": 
     *     { "b_no": "1234567890" }
     *   
     * }
     * }</pre>
     * </p>
     * 
     * @param request {@link BusinessDTO} 객체로, 사업자 등록번호 리스트를 포함합니다.
     * @return 사업자 상태 확인 결과를 포함한 {@link ResponseEntity}
     * 
     * <ul>
     *     <li>400 Bad Request: 사업자번호가 누락된 경우</li>
     *     <li>200 OK: 사업자 상태 조회 성공</li>
     *     <li>500 Internal Server Error: 예외 발생 시</li>
     * </ul>
     */
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
