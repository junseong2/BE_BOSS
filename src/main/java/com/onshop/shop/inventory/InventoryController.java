package com.onshop.shop.inventory;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/inventories")
public class InventoryController {
	
	
	@GetMapping()
	public ResponseEntity<?> getAllInventory(
			@RequestParam Long page, @RequestParam Long size
			){
		
		return null;
		
	}
}
