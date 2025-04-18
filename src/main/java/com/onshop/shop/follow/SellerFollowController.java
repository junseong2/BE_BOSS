package com.onshop.shop.follow;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.security.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/user-info/follow")
public class SellerFollowController {
	
	private final JwtUtil jwtUtil;
	private final SellerFollowService sellerFollowService;
	
	
	@PostMapping()
	public ResponseEntity<?> toggleFollow(   
		@Valid @RequestBody SellerStoreName storeNameDTO,
		@CookieValue(value = "jwt", required = false) String token) {
		
	    // JWT 토큰이 없으면 로그인하지 않은 상태
	    if (token == null) {
	        return ResponseEntity.status(401).body(Map.of("error", "로그인 정보가 없습니다."));
	    }

	    // JWT에서 userId 추출
	    Long userId = jwtUtil.extractUserId(token);
	    
	    SellerFollowStateDTO state = sellerFollowService.toggleFollow(userId, storeNameDTO.getStoreName());
	    
	    
	    return ResponseEntity.ok(state);

	}
	
	

}
