package com.onshop.shop.settlement;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
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

import com.onshop.shop.exception.NotAuthException;
import com.onshop.shop.security.JwtUtil;
import com.onshop.shop.seller.SellerStatsDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/settlements")
@CrossOrigin(origins = "http://localhost:5173")
public class SettlementController {
	
	private final SettlementService settlementService;
	private final JwtUtil jwtUtil;
	/**관리자 */
	
	
	
	
	/**판매자 */
	// 정산 요청
	@PostMapping("/seller/request")
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
	@GetMapping("/seller/check")
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
	
	// 관리자에서 정산 요청 조회
	@GetMapping("/admin/check")
	public ResponseEntity<?> getAdminSettlement(@RequestParam String status) {
	    List<Settlement> settlements = settlementService.getAdminSettlementsByStatus(status);

//	    if (settlements.isEmpty()) {
//	        return ResponseEntity.status(404).body("정산 요청이 없습니다.");
//	    }

	    return ResponseEntity.ok(settlements);
	}


	
	@PatchMapping("/admin/{settlementId}/status")
	public ResponseEntity<?> updateSettlementStatus(
	        @PathVariable Long settlementId,
	        @RequestParam String status
	) {
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
	
	@GetMapping("/admin/stats")
	public ResponseEntity<SettlementStatsDTO> getSettlementStats() {
	    SettlementStatsDTO stats = settlementService.getSettlementStats();
	    return ResponseEntity.ok(stats);
	}

}
