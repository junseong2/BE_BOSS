package com.onshop.shop.domain.product.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.domain.product.dto.ProductDetailResponseDTO;
import com.onshop.shop.domain.product.dto.ProductsDTO;
import com.onshop.shop.domain.product.entity.Product;
import com.onshop.shop.domain.product.service.ProductsBuyerService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductsBuyerController {

    private final ProductsBuyerService productsService;
    
    // 모든 상품 조회
    @GetMapping
    public List<ProductsDTO> getAllProducts(
        @RequestParam("page") int page,
        @RequestParam("size") int size) {
        return productsService.getAllProducts(page, size);
    }
    
    // 인기 상품 조회
    @GetMapping("/popular")
    public ResponseEntity<List<Product>> getPopularProducts(
        @RequestParam("sortBy") String sortBy,
        @RequestParam("page") int page,
        @RequestParam("size") int size) {
        
        log.info("인기 상품 조회 요청: sortBy={}", sortBy);
        
        List<Product> products;
        
        switch (sortBy.toLowerCase()) {
            case "daily":
                products = productsService.getPopularProductsDaily(page, size);
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
    @GetMapping("/{productId}")
    public ProductsDTO getProductById(
    		@PathVariable("productId") Long productId) {
        log.info("상품 조회: productId={}", productId);
        return productsService.getProductById(productId);
    }

    // 특정 카테고리의 상품 조회
    @GetMapping("/category/{categoryId}")
    public List<ProductsDTO> getProductsByCategory(
        @PathVariable("categoryId") Long categoryId,
        @RequestParam("page") int page,
        @RequestParam("size") int size) {
        return productsService.getProductsByCategory(categoryId, page, size);
    }

    // 상품 검색
    @GetMapping("/search")
    public List<ProductsDTO> searchProducts(@RequestParam("query") String query) {
        return productsService.searchProducts(query);
    }

    // 상품 상세 조회
    @GetMapping("/detail/{productId}")
    public ResponseEntity<ProductDetailResponseDTO> getProductDetail(@PathVariable("productId") Long productId) {
        ProductDetailResponseDTO dto = productsService.getProductDetail(productId);
        return ResponseEntity.ok(dto);
    }
}