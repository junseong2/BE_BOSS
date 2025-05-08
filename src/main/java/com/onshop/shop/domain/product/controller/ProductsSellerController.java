package com.onshop.shop.domain.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
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
import com.onshop.shop.domain.product.dto.SellerProductIdsDTO;
import com.onshop.shop.domain.product.dto.SellerProductsDTO;
import com.onshop.shop.domain.product.dto.SellerProductsRequestDTO;
import com.onshop.shop.domain.product.dto.SellerProductsResponseDTO;
import com.onshop.shop.domain.product.entity.Product;
import com.onshop.shop.domain.product.service.ProductsSellerService;
import com.onshop.shop.global.exception.NotAuthException;
import com.onshop.shop.global.exception.SuccessMessageResponse;
import com.onshop.shop.global.util.JwtUtil;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/seller/products")
@RequiredArgsConstructor
@Slf4j
public class ProductsSellerController {

    private final ProductsSellerService productsService;
    private final JwtUtil jwtUtil;
    
    // 판매자 대시보드 상품 조회
    @GetMapping("/dashboard")
    public ResponseEntity<?> getAllDashboardProducts(
        @RequestParam("page") int page,
        @RequestParam("size") int size,
        @RequestParam(required = false, defaultValue = "") String search,
        @CookieValue(value = "jwt", required = false) String token) {

        if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
        
        SellerProductsResponseDTO products = productsService.getAllDashboardProducts(
            page, size, search, userId );

        return ResponseEntity.ok(products);
    }

    // 판매자 상품 추가(단일)
    @PostMapping
    public ResponseEntity<?> registerProduct(
        @Valid @RequestParam("product") String productJSON,
        @RequestParam(value ="images", required = false) List<MultipartFile> images,
        @CookieValue(value = "jwt", required = false) String token) throws JsonMappingException, JsonProcessingException {

        if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
        
        // JSON 문자열을 Product 객체로 변환
        ObjectMapper objectMapper = new ObjectMapper();
        SellerProductsRequestDTO productDTO = objectMapper.readValue(productJSON, SellerProductsRequestDTO.class);
        
        log.info("productDTO:{}", productDTO);
        
        Product savedProduct = productsService.registerProduct(productDTO, userId);
        
        if (images != null) {
            productsService.registerProductImages(images, savedProduct);
        }
        
        return ResponseEntity.created(null).body(null);
    }

    // 판매자 상품 수정
    @PatchMapping("/{productId}")
    public ResponseEntity<?> updateProduct(
        @PathVariable Long productId,
        @Valid @RequestParam("product") String productJSON,
        @RequestParam(value = "images", required = false) List<MultipartFile> images,
        @CookieValue(value = "jwt", required = false) String token) throws JsonMappingException, JsonProcessingException {

        if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);

        ObjectMapper objectMapper = new ObjectMapper();
        SellerProductsRequestDTO productDTO = objectMapper.readValue(productJSON, SellerProductsRequestDTO.class);
        
        log.info("productDTO:{}", productDTO);
        
        Product updatedProduct = productsService.updateProducts(productId, productDTO, userId);
        
        if (images != null) {
            productsService.registerProductImages(images, updatedProduct);
        }

        SuccessMessageResponse response = new SuccessMessageResponse(
            HttpStatus.OK, 
            "선택 상품의 정보가 수정되었습니다.",
            new SellerProductsDTO(productId, productDTO.getName(), productDTO.getPrice(), productDTO.getCategoryName(), productDTO.getStock())
        );
        
        return ResponseEntity.ok(response);
    }

    // 판매자 상품 삭제
    @DeleteMapping
    public ResponseEntity<?> removeProducts(
        @Valid @RequestBody SellerProductIdsDTO productIds,
        @CookieValue(value = "jwt", required = false) String token) {
        
        if (token == null) {
            throw new NotAuthException("요청 권한이 없습니다.");
        }

        Long userId = jwtUtil.extractUserId(token);
        
        productsService.removeProducts(productIds, userId);
        
        return ResponseEntity.noContent().build();
    }

    // 판매자 상품 CSV 업로드
    @PostMapping("/csv")
    public ResponseEntity<?> uploadCsv(
        @RequestParam("file") MultipartFile file,
        @CookieValue(value = "jwt", required = false) String token) {
        
        try {
            productsService.uploadProductsCsv(file, null); // CSV 업로드 처리
            return ResponseEntity.ok("CSV 파일 업로드 완료");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }
}
