package com.onshop.shop.seller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
@RequestMapping("/seller/products")
@Slf4j
public class SellerProductsController {
	
	private final SellerProductsService sellerProductsService;
	
	
	// 모든 상품 조회
	@GetMapping()
	public ResponseEntity<?> getAllProducts(
			@RequestParam int page,
			@RequestParam int size
			){
		
		List<SellerProductsDTO> products = sellerProductsService.getAllProducts(page, size);
		return ResponseEntity.ok(products);
	}
	
	// 상품 추가
	@PostMapping()
	public ResponseEntity<?> registerProduct(
			@Valid @RequestBody List<SellerProductsRequestDTO> products
			){
		sellerProductsService.registerProducts(products);
		
		
		return ResponseEntity.created(null).body(null);
	}
	
	// 상품 삭제(단일 삭제)
	@DeleteMapping("/{productId}")
	public ResponseEntity<?> removeProduct(
			@PathVariable Long productId
			){
		
		if(productId == null) {
			throw new NullPointerException("상품 ID 가 NULL 입니다"+"(입력값:"+ productId+").");
		}
		
		log.info("productId:{}", productId);
		
		sellerProductsService.removeProduct(productId);
		
		return ResponseEntity.noContent().build();
	}
	
	// 상품 삭제(다중 삭제)
	@DeleteMapping()
	public ResponseEntity<?> removeProducts(
			@Valid @RequestBody SellerProductIdsDTO productIds
			){
		log.info("ids:{}", productIds);
		sellerProductsService.removeProducts(productIds);
		
		return ResponseEntity.noContent().build();
		
	}
}
