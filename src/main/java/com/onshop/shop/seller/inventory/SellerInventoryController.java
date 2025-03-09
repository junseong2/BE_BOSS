package com.onshop.shop.seller.inventory;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
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
	public ResponseEntity<?> getAllInventory(
			@RequestParam int page, @RequestParam int size
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
}
