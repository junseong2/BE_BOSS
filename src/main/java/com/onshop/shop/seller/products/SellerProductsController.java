package com.onshop.shop.seller.products;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onshop.shop.exception.SuccessMessageResponse;
import com.onshop.shop.products.Product;

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
	
	// 상품 추가(다중)
	@PostMapping("/multiple")
	public ResponseEntity<?> registerProducts(
			@Valid @RequestParam List<SellerProductsRequestDTO> productsDTO
			){
		
		log.info("productsDTO:{}", productsDTO);
		sellerProductsService.registerProducts(productsDTO);
		
		
		return ResponseEntity.created(null).body(null);
	}
	
	// 상품 추가(
	@PostMapping()
	public ResponseEntity<?> registerProduct(
			@Valid @RequestParam("product") String productJSON,
			@RequestParam("images") List<MultipartFile> images
			) throws JsonMappingException, JsonProcessingException{
		
		
	       // JSON 문자열을 Product 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        SellerProductsRequestDTO productDTO = objectMapper.readValue(productJSON, SellerProductsRequestDTO.class);
        
        Product savedProduct = sellerProductsService.registerProduct(productDTO);
        
        sellerProductsService.reigsterProductImages(images, savedProduct);
		
		return ResponseEntity.created(null).body(null);
	}
	
	// 상품 검색
	@GetMapping("/search")
	public ResponseEntity<?> searchProduct(
			@RequestParam String search,
			@RequestParam int page,
			@RequestParam int size
			){
		
		log.info("search:{}, page:{}, size:{}", search, page,size);
		List<SellerProductsDTO> products = sellerProductsService.searchProducts(search, page, size);
		return ResponseEntity.ok(products);
		
	}
	
	
	
	// 상품 수정
	@PatchMapping("/{productId}")
	public ResponseEntity<?> updateProduct(
			@PathVariable Long productId,
			@Valid @RequestBody SellerProductsRequestDTO productDTO
			){
		if(productId == null) {
			throw new NullPointerException("상품ID는 필수입니다.");
		}
		
		sellerProductsService.updateProducts(productId, productDTO);
		

		SuccessMessageResponse response = new SuccessMessageResponse(
				HttpStatus.OK, 
				"선택 상품의 정보가 수정되었습니다.", 		
				SellerProductsResponseDTO.builder()
					.category(productDTO.getCategoryName())
					.productName(productDTO.getName())
					.price(productDTO.getPrice())
					.stock(productDTO.getStock())
					.build());
		return ResponseEntity.ok(response);
	}
	
	// 상품 삭제(단일, 다중 모두 처리)
	@DeleteMapping()
	public ResponseEntity<?> removeProducts(
			@Valid @RequestBody SellerProductIdsDTO productIds
			){
		log.info("ids:{}", productIds);
		sellerProductsService.removeProducts(productIds);
		
		return ResponseEntity.noContent().build();
		
	}

}
