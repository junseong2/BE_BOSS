package com.onshop.shop.store;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.product.Product;
import com.onshop.shop.product.ProductsService;
import com.onshop.shop.security.JwtUtil;

@RestController
@RequestMapping("/seller")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SellerController {


    @Autowired
    private SellerService sellerService;

    @Autowired
    private JwtUtil jwtUtil; // ✅ JWT 유틸리티 추가

    // ✅ Seller 정보 가져오기 (JWT 인증 기반, storename으로 검색)
    @GetMapping("/info/{storename}")
    public ResponseEntity<Map<String, Object>> getSellerInfoByStoreName(
            @PathVariable String storename,
            @CookieValue(value = "jwt", required = false) String token) {
        
       

        // ✅ 판매자 정보 조회
        Optional<Seller> sellerOptional = sellerService.getSellerByStorename(storename);
        if (sellerOptional.isPresent()) {
            Seller seller = sellerOptional.get();

            // 🔥 ✅ `userId` 검증 제거 (누구나 판매자 정보 조회 가능)
            Map<String, Object> response = Map.of(
                    
            		"storename", seller.getStorename(),
                    "sellerId",seller.getSellerId(),
                    "headerId", seller.getHeaderId(),
                    "menuBarId", seller.getMenuBarId(),
                    "navigationId", seller.getNavigationId(),
                    "seller_menubar_color", seller.getSellerMenubarColor()
            );

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(404).body(Map.of("error", "판매자를 찾을 수 없습니다."));
    }
    
    @Autowired
    private ProductsService productsService; // ✅ 올바른 Service 주입


    
    
    
    
    // 특정 판매자의 제품 목록 조회
    @GetMapping("/product")
    public ResponseEntity<Map<String, Object>> getProductsBySeller(
            @RequestParam Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        try {
            Pageable pageable = PageRequest.of(page, size, 
                sort.equals("asc") ? Sort.by("price").ascending() : Sort.by("price").descending());

            Page<Product> productsPage = productsService.getProductsBySeller(sellerId, pageable);

            if (productsPage.isEmpty()) {
                return ResponseEntity.noContent().build(); // 상품이 없을 경우 204 응답
            }

            // ✅ 페이징 데이터를 포함하여 반환
            Map<String, Object> response = new HashMap<>();
            response.put("products", productsPage.getContent()); // 실제 상품 데이터
            response.put("currentPage", productsPage.getNumber()); // 현재 페이지 번호
            response.put("totalItems", productsPage.getTotalElements()); // 전체 상품 개수
            response.put("totalPages", productsPage.getTotalPages()); // 전체 페이지 수
            response.put("sortOrder", sort); // 정렬 방식

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // ✅ 로그 출력하여 디버깅
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류 발생"));
        }
    }
}
