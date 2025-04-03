package com.onshop.shop.settlement;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.exception.NotAuthException;
import com.onshop.shop.security.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
public class SettlementController {
	
	private final SettlementService settlementService;
	private final JwtUtil jwtUtil;
	/**관리자 */
	
	
	/**판매자 */
	// 정산 요청
	@PostMapping("/seller/settlements")
	public ResponseEntity<?> requestSettlment(
			@Valid @RequestBody SettlementRequestDTO requestDTO,
			@CookieValue(value = "jwt", required = false) String token) {
        if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        if(!jwtUtil.validateToken(token)) {
        	throw new NotAuthException("요청 권한이 없습니다.");
        }
        
        Long userId = jwtUtil.extractUserId(token);
		
		log.info("sett:{}", requestDTO);
		
		
		SettlementsDTO settlements= settlementService.requestSettlement(requestDTO, userId);
		
		
		return ResponseEntity.ok(settlements);
		
	}
	
	// 정산 조회
	@GetMapping("/seller/settlements")
	public ResponseEntity<?> getSettlement(
			@RequestParam() int page,
			@RequestParam() int size,
			@RequestParam() LocalDate startDate,
			@RequestParam() LocalDate endDate,
			@RequestParam() String username,
			@RequestParam() Optional<Long> settlementId,
			@CookieValue(value = "jwt", required = false) String token) {
        if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }
        
        if(!jwtUtil.validateToken(token)) {
        	throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
        
		SettlementResponseDTO response= settlementService.getSettlements(page, size, userId, SettlementSearchConditionDTO.builder()
				.startDate(startDate.atStartOfDay())
				.endsDate(endDate.atTime(23,59,59))
				.username(username)
				.settlementId(settlementId)
				.build() );
		
		return ResponseEntity.ok(response);
	}
	
	// 판매자 상태 변경
    @PatchMapping("/admin/seller/{sellerId}/update-status")
    public ResponseEntity<String> updateSellerStatus(
            @PathVariable Long sellerId, 
            @RequestParam String status, 
            @CookieValue(value = "jwt", required = false) String token) {

        // JWT 토큰 검증
        if (token == null || !jwtUtil.validateToken(token)) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        try {
            settlementService.updateSellerStatus(sellerId, status);  // 상태 변경
            return ResponseEntity.ok("상태 변경 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("상태 변경 실패");
        }
    }
    
    @GetMapping
    public ResponseEntity<SettlementResponseDTO> getAllSettlements(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long settlementId,
            @RequestParam(required = false) String username
    ) {
        SettlementSearchConditionDTO condition = SettlementSearchConditionDTO.builder()
                .startDate(LocalDateTime.now().minusMonths(3))  // 기본 최근 3개월
                .endsDate(LocalDateTime.now())
                .settlementId(Optional.ofNullable(settlementId))
                .username(username)
                .build();

        SettlementResponseDTO result = settlementService.getAllSettlements(page, size, condition);
        return ResponseEntity.ok(result);
    }


}
