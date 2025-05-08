package com.onshop.shop.domain.settlement.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.domain.settlement.dto.SettlementRequestDTO;
import com.onshop.shop.domain.settlement.dto.SettlementResponseDTO;
import com.onshop.shop.domain.settlement.dto.SettlementSearchConditionDTO;
import com.onshop.shop.domain.settlement.dto.SettlementStatsDTO;
import com.onshop.shop.domain.settlement.dto.SettlementsDTO;
import com.onshop.shop.domain.settlement.entity.Settlement;
import com.onshop.shop.domain.settlement.enums.SettlementStatus;
import com.onshop.shop.domain.settlement.service.SettlementService;
import com.onshop.shop.global.exception.NotAuthException;
import com.onshop.shop.global.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 정산 관련 요청을 처리하는 컨트롤러입니다.
 * 판매자와 관리자의 정산 요청, 조회, 상태 변경 등을 담당합니다.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/settlements")
@CrossOrigin(origins = "http://localhost:5173")
public class SettlementController {

    private final SettlementService settlementService;
    private final JwtUtil jwtUtil;

    /**
     * 판매자의 정산 요청을 처리합니다.
     *
     * @param requestDTO 정산 요청 데이터
     * @param token 사용자 인증을 위한 JWT 쿠키
     * @return 정산 결과 DTO
     * @throws NotAuthException 인증되지 않은 사용자일 경우 발생
     */
    @PostMapping("/seller/request")
    public ResponseEntity<?> requestSettlment(
            @Valid @RequestBody SettlementRequestDTO requestDTO,
            @CookieValue(value = "jwt", required = false) String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
        log.info("sett:{}", requestDTO);

        SettlementsDTO settlements = settlementService.requestSettlement(requestDTO, userId);
        return ResponseEntity.ok(settlements);
    }

    /**
     * 판매자의 정산 내역을 조회합니다.
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param startDate 검색 시작일
     * @param endDate 검색 종료일
     * @param username 사용자 이름
     * @param settlementId 정산 ID (Optional)
     * @param token 사용자 인증을 위한 JWT 쿠키
     * @return 정산 목록 및 전체 개수 DTO
     * @throws NotAuthException 인증되지 않은 사용자일 경우 발생
     */
    @GetMapping("/seller/check")
    public ResponseEntity<?> getSettlement(
            @RequestParam int page,
            @RequestParam int size,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam String username,
            @RequestParam Optional<Long> settlementId,
            @CookieValue(value = "jwt", required = false) String token) {
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);

        SettlementResponseDTO response = settlementService.getSettlements(page, size, userId,
                SettlementSearchConditionDTO.builder()
                        .startDate(startDate.atStartOfDay())
                        .endsDate(endDate.atTime(23, 59, 59))
                        .username(username)
                        .settlementId(settlementId)
                        .build());

        return ResponseEntity.ok(response);
    }

    /**
     * 관리자가 상태에 따라 정산 요청 목록을 조회합니다.
     *
     * @param status 조회할 정산 상태
     * @return 상태에 해당하는 정산 목록
     */
    @GetMapping("/admin/check")
    public ResponseEntity<?> getAdminSettlement(@RequestParam String status) {
        List<Settlement> settlements = settlementService.getAdminSettlementsByStatus(status);
        return ResponseEntity.ok(settlements);
    }

    /**
     * 관리자가 정산 상태를 변경합니다.
     *
     * @param settlementId 정산 ID
     * @param status 변경할 상태 값
     * @return 상태 변경 결과 메시지
     */
    @PatchMapping("/admin/{settlementId}/status")
    public ResponseEntity<?> updateSettlementStatus(
            @PathVariable Long settlementId,
            @RequestParam String status) {
        try {
            SettlementStatus newStatus = SettlementStatus.valueOf(status);
            settlementService.updateSettlementStatus(settlementId, newStatus);
            return ResponseEntity.ok("정산 상태가 업데이트되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("잘못된 상태값입니다.");
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("정산 상태 업데이트 실패: " + e.getMessage());
        }
    }

    /**
     * 관리자용 전체 정산 통계 정보를 조회합니다.
     *
     * @return 총 건수, 상태별 건수 통계 DTO
     */
    @GetMapping("/admin/stats")
    public ResponseEntity<SettlementStatsDTO> getSettlementStats() {
        SettlementStatsDTO stats = settlementService.getSettlementStats();
        return ResponseEntity.ok(stats);
    }
}
