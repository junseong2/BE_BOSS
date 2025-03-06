package com.onshop.shop.products;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.onshop.shop.products.*;

import lombok.RequiredArgsConstructor;

import com.onshop.shop.category.*;


import com.onshop.shop.category.Category;
import com.onshop.shop.category.CategoryService;

import java.util.List;
import org.springframework.web.bind.annotation.CrossOrigin;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductsController {

    private final ProductsService productsService;

    // 모든 상품 조회
    @GetMapping
    public List<ProductsDTO> getAllProducts() {
        return productsService.getAllProducts();
    }

    // 단일 상품 조회
    @GetMapping("/{productId}")
    public ProductsDTO getProductById(@PathVariable Long productId) {
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
}