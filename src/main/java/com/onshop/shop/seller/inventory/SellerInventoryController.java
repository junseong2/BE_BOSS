package com.onshop.shop.seller.inventory;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/inventories")
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
}
