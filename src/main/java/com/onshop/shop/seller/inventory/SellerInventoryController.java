package com.onshop.shop.seller.inventory;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/inventories")
@Slf4j
public class SellerInventoryController {
	
	private final SellerInventoryService sellerInventoryService;
	
	
	// 상품 재고 조회
	@GetMapping()
	@Validated
	public ResponseEntity<?> getAllInventory(
			@RequestParam("page") @Min(value = 0, message="page는 최소 0 이상이어야 합니다.") int page,
			@RequestParam("size") @Min(value = 5, message="size는 최소 5 이상이어야 합니다.") int size
			){
		List<SellerInventoryResponseDTO> inventories = sellerInventoryService.getAllInventory(page, size);
		return ResponseEntity.ok(inventories);
	}
	
	// 상품 재고 발주(간이 즉시 요청)
	@PatchMapping()
	public ResponseEntity<?> addInventory(
			@Valid @RequestBody List<InventoryOrderRequestDTO> orderRequest
			){
		

		log.info("order:{}", orderRequest);
		sellerInventoryService.updateInventory(orderRequest);
		
		return ResponseEntity.ok().build();
	}
	
	// 상품 재고 검색
	@GetMapping("/search")
	@Validated
	public ResponseEntity<?> searchInventories(
			@RequestParam("search") @NotBlank(message="검색어는 필수입니다.") String search,
			@RequestParam("page") @Min(value = 0, message="page는 최소 0 이상이어야 합니다.") int page,
			@RequestParam("size") @Min(value = 5, message="size는 최소 5 이상이어야 합니다.") int size
			){
		
		List<SellerInventoryResponseDTO> inventories = sellerInventoryService.searchInventories(search, page, size);
		return ResponseEntity.ok(inventories);
	}
}
