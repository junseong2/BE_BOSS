package com.onshop.shop.seller.productManagement;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/saller/products")
@RequiredArgsConstructor
public class ProductManagementController {
	
	@GetMapping()
	public ResponseEntity<?> getProducts(){
		
		return null;
	}

}
