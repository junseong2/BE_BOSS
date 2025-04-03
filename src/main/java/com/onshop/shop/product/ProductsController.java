package com.onshop.shop.product;

import java.awt.print.Pageable;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
import com.onshop.shop.category.CategoryDTO;
import com.onshop.shop.exception.SuccessMessageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("")
@RequiredArgsConstructor
@Slf4j
public class ProductsController {

    private final ProductsService productsService;

    /** 구매자*/
    // 모든 상품 조회
    @GetMapping("/products")
    public List<ProductsDTO> getAllProducts() {
        return productsService.getAllProducts();
    }
    
    
    @GetMapping("/products/popular")
    public ResponseEntity<List<Product>> getPopularProducts(@RequestParam String sortBy) {
        log.info("인기 상품 조회 요청: sortBy={}", sortBy);
        
        List<Product> products;
        
        switch (sortBy.toLowerCase()) {
            case "daily":
                products = productsService.getPopularProductsDaily();
                break;
            case "weekly":
                products = productsService.getPopularProductsWeekly();
                break;
            case "monthly":
                products = productsService.getPopularProductsMonthly();
                break;
            case "all":
                products = productsService.getAllPopularProducts();
                break;
            default:
                return ResponseEntity.badRequest().body(null);
        }

        return ResponseEntity.ok(products);
    }

    // 단일 상품 조회
    @GetMapping("/products/{productId}")
    public ProductsDTO getProductById(@PathVariable Long productId) {
    	
    	log.info("products id:{}", productId);
    	
        return productsService.getProductById(productId);
    }
    
    // ✅ 특정 카테고리의 상품 조회 API 추가
    @GetMapping("/products/category/{categoryId}")
    public List<ProductsDTO> getProductsByCategory(@PathVariable Long categoryId) {
        return productsService.getProductsByCategory(categoryId);
    }
    @GetMapping("/seller/used-categories")
    public ResponseEntity<List<CategoryDTO>> getUsedCategories(@RequestParam Long sellerId) {
        List<CategoryDTO> usedCategories = productsService.getUsedCategoriesBySeller(sellerId);
        return ResponseEntity.ok(usedCategories);
    }
    @GetMapping("/products/search")
    public List<ProductsDTO> searchProducts(@RequestParam String query) {
        return productsService.searchProducts(query);
    }
    
    
    /** 판매자 */
	// 모든 상품 조회
    @GetMapping("/seller/products")
    public ResponseEntity<?> getAllProducts(
        @RequestParam Long sellerId,
        @RequestParam int page,
        @RequestParam int size,
        @RequestParam(required = false, defaultValue = "") String search,
        @RequestParam(required = false, defaultValue = "recommend") String sort,
        @RequestParam(required = false) Long categoryId
    ) {
        log.info("📦 Seller ID: {}, 검색어: '{}', 정렬: {}, 카테고리: {}", sellerId, search, sort, categoryId);

        // ✅ categoryId도 넘겨줍니다!
        SellerProductsResponseDTO products = productsService.getAllProducts(
            sellerId, page, size, search, sort, categoryId
        );

        return ResponseEntity.ok(products);
    }

	
	// 상품 추가(다중)
	@PostMapping("/seller/products/multiple")
	public ResponseEntity<?> registerProducts(
			@Valid @RequestParam List<SellerProductsRequestDTO> productsDTO
			){
		
		log.info("productsDTO:{}", productsDTO);
		productsService.registerProducts(productsDTO);
		
		
		return ResponseEntity.created(null).body(null);
	}
	
	// 상품 추가(
	@PostMapping("/seller/products")
	public ResponseEntity<?> registerProduct(
//			@CookieValue(value = "jwt", required = false) String token,
			@Valid @RequestParam("product") String productJSON,
			@RequestParam("images") List<MultipartFile> images
			) throws JsonMappingException, JsonProcessingException{
		
		
	       // JSON 문자열을 Product 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        SellerProductsRequestDTO productDTO = objectMapper.readValue(productJSON, SellerProductsRequestDTO.class);
        
        Product savedProduct = productsService.registerProduct(productDTO);
        
        productsService.reigsterProductImages(images, savedProduct);
		
		return ResponseEntity.created(null).body(null);
	}
	
	
	
	// 상품 수정
	@PatchMapping("/seller/products/{productId}")
	public ResponseEntity<?> updateProduct(
//			@CookieValue(value = "jwt", required = false) String token,
			@PathVariable Long productId,
			@Valid @RequestBody SellerProductsRequestDTO productDTO
			){
		if(productId == null) {
			throw new NullPointerException("상품ID는 필수입니다.");
		}
		
		productsService.updateProducts(productId, productDTO);
		

		SuccessMessageResponse response = new SuccessMessageResponse(
				HttpStatus.OK, 
				"선택 상품의 정보가 수정되었습니다.", 		
				SellerProductsDTO.builder()
					.productId(productId)
					.categoryName(productDTO.getCategoryName())
					.name(productDTO.getName())
					.price(productDTO.getPrice())
					.stock(productDTO.getStock())
					.build());
		return ResponseEntity.ok(response);
	}
	
	// 상품 삭제(단일, 다중 모두 처리)
	@DeleteMapping("/seller/products")
	public ResponseEntity<?> removeProducts(
//			@CookieValue(value = "jwt", required = false) String token,
			@Valid @RequestBody SellerProductIdsDTO productIds
			){
		log.info("ids:{}", productIds);
		productsService.removeProducts(productIds);
		
		return ResponseEntity.noContent().build();
		
	}
	@GetMapping("/seller/products/popular")
	public ResponseEntity<List<SellerProductsDTO>> getSellerPopularProducts(
	        @RequestParam Long sellerId,
	        @RequestParam String sortBy
	) {
	    List<SellerProductsDTO> result = productsService.getPopularProductsBySeller(sellerId, sortBy);
	    return ResponseEntity.ok(result);
	}


}