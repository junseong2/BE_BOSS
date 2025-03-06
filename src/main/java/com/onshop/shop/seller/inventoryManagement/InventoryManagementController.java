package com.onshop.shop.seller.inventoryManagement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/seller")
public class InventoryManagementController {
	
	
	// 인벤토리 조회
	@GetMapping
	public ResponseEntity<?> getInventory(
			@RequestParam() int page,
			@RequestParam() int size
			){
			
		return null;
	}

}
