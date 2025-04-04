package com.onshop.shop.inventory;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.exception.NotAuthException;
import com.onshop.shop.security.JwtUtil;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/inventories")
@Slf4j
public class InventoryController {
	
	private final InventoryService inventoryService;
	private final JwtUtil jwtUtil;
	
	
	// 상품 재고 조회
	@GetMapping()
	@Validated
	public ResponseEntity<SellerInventoryResponseDTO> getAllInventory(
			@RequestParam("page") @Min(value = 0, message="page는 최소 0 이상이어야 합니다.") int page,
			@RequestParam("size") @Min(value = 5, message="size는 최소 5 이상이어야 합니다.") int size,
			@RequestParam String search,
			@RequestParam  @Pattern(regexp = "soldout|warn|all", message = "'soldout', 'warn', 'all' 중 하나이어야 합니다.")  String state,
			@CookieValue(value = "jwt", required = false) String token) {
        
		if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token); // ✅ JWT에서 userId 추출

		SellerInventoryResponseDTO inventory = inventoryService.getAllInventory(page, size, search,state, userId);
		return ResponseEntity.ok(inventory);
	}
	
	// 상품 재고 추가
	@PatchMapping()
	public ResponseEntity<?> addInventory(
			@Valid @RequestBody List<InventoryOrderRequestDTO> orderRequest,
			@CookieValue(value = "jwt", required = false) String token) {
        
		if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token); // ✅ JWT에서 userId 추출

		
		
		log.info("order:{}", orderRequest);
		inventoryService.updateInventory(orderRequest, userId);
		
		return ResponseEntity.ok().build();
	}
}

