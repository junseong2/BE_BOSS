package com.onshop.shop.products;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductsController {

    private final ProductsService productsService;

    private final ChatGPTService chatGPTService;

    // 모든 상품 조회
    @GetMapping
    public List<ProductsDTO> getAllProducts() {
        return productsService.getAllProducts();
    }

    // 단일 상품 조회
    @GetMapping("/{productId}")
    public ProductsDTO getProductById(@PathVariable Long productId) {
    	log.info("products id:{}", productId);
        return productsService.getProductById(productId);
    }

    // ✅ 특정 카테고리의 상품 조회 API 추가
    @GetMapping("/category/{categoryId}")
    public List<ProductsDTO> getProductsByCategory(@PathVariable Long categoryId) {
        return productsService.getProductsByCategory(categoryId);
    }

    @GetMapping("/search")
    public List<ProductsDTO> searchProducts(@RequestParam String query) {
        return productsService.searchProducts(query);
    }

//    @PostMapping("/recommend-text")
//    public List<ProductsDTO> recommendByText(@RequestBody Map<String, String> request) {
//        String userMessage = request.get("message"); // 사용자 입력 가져오기
//        return chatGPTService.getRecommendation(userMessage); // AI 추천 결과 반환
//    }

    @PostMapping("/recommend-text")
    public ResponseEntity<Map<String, Object>> recommendProduct(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");

        // ChatGPT를 통해 추천 상품 리스트 가져오기
        List<Map<String, Object>> recommendations = chatGPTService.processUserQuery(userMessage);

        // JSON 응답 생성
        Map<String, Object> response = new HashMap<>();
        response.put("products", recommendations);
        return ResponseEntity.ok(response);
    }
}