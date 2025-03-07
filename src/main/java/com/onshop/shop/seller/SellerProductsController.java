package com.onshop.shop.seller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/seller/products")
public class SellerProductsController {
	
	private final SellerProductsService sellerProductsService;
	
	
	@GetMapping()
	public ResponseEntity<?> getAllProducts(
			@RequestParam int page,
			@RequestParam int size
			){
		
		List<SellerProductsDTO> products = sellerProductsService.getAllProducts(page, size);
		return ResponseEntity.ok(products);
	}
	
	@PostMapping()
	public ResponseEntity<?> registerProduct(
			@Valid @RequestBody List<SellerProductsRequestDTO> products
			){
		sellerProductsService.registerProducts(products);
		
		
		return ResponseEntity.created(null).body(null);
	}
}
