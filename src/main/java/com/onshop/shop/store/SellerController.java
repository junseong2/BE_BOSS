package com.onshop.shop.store;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.products.Product;
import com.onshop.shop.products.ProductsService;
import com.onshop.shop.security.JwtUtil;

@RestController
@RequestMapping("/seller")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true")
public class SellerController {

    private static final Logger logger = LoggerFactory.getLogger(SellerController.class);

    @Autowired
    private SellerService sellerService;

    @Autowired
    private JwtUtil jwtUtil; // ✅ JWT 유틸리티 추가

    // ✅ Seller 정보 가져오기 (JWT 인증 기반, storename으로 검색)
    @GetMapping("/info/{storename}")
    public ResponseEntity<Map<String, Object>> getSellerInfoByStoreName(
            @PathVariable String storename,
            @CookieValue(value = "jwt", required = false) String token) {

        logger.info("📌 [SellerController] /info/{} 요청 수신됨", storename);

        // ✅ 판매자 정보 조회
        Optional<Seller> sellerOptional = sellerService.getSellerByStorename(storename);
        if (sellerOptional.isPresent()) {
            Seller seller = sellerOptional.get();
            logger.info("✅ [SellerController] 판매자 정보 조회 성공: {}", seller.getStorename());

            // 🔥 ✅ `userId` 검증 제거 (누구나 판매자 정보 조회 가능)
            Map<String, Object> response = Map.of(

            		"storename", seller.getStorename(),
                    "sellerId",seller.getSellerId(),
                    "headerId", seller.getHeaderId(),
                    "menuBarId", seller.getMenuBarId(),
                    "navigationId", seller.getNavigationId()
            );

            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(404).body(Map.of("error", "판매자를 찾을 수 없습니다."));
    }

    @Autowired
    private ProductsService productsService; // ✅ 올바른 Service 주입


    // 특정 판매자의 제품 목록 조회
    @GetMapping("/product")
    public List<Product> getProductsBySeller(@RequestParam Long sellerId) {



        return productsService.getProductsBySellerId(sellerId);
    }

}
