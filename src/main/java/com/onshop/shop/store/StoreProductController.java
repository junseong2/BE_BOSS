package com.onshop.shop.store;

import java.util.List;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.product.Product;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
public class StoreProductController {

    private final StoreProductService storeProductService;
    private final SellerService sellerService;

    public StoreProductController(StoreProductService storeProductService, SellerService sellerService) {
        this.storeProductService = storeProductService;
        this.sellerService = sellerService;
    }

    // ✅ 판매자 정보 반환
    @GetMapping("/store/{storeName}/products/info")
    public ResponseEntity<?> getSellerByStorename(@PathVariable String storeName) {
        System.out.println("✅ 요청된 storename: " + storeName); // 디버깅용 로그

        Optional<Seller> seller = sellerService.getSellerByStorename(storeName);

        if (seller.isEmpty()) {
            System.out.println("❌ 판매자 없음: " + storeName);
            return ResponseEntity.status(404).body("판매자를 찾을 수 없습니다: " + storeName);
        }

        System.out.println("✅ 판매자 조회 성공: " + seller.get());
        return ResponseEntity.ok(seller.get());
    }

    // ✅ 상품 목록 반환
    @GetMapping("/store/{storeName}/products")
    public ResponseEntity<?> getProductsByStore(@PathVariable String storeName) {
        System.out.println("✅ 상품 조회 요청 storename: " + storeName);

        // 1️⃣ 판매자 정보 조회
        Optional<Seller> sellerOpt = sellerService.getSellerByStorename(storeName);
        if (sellerOpt.isEmpty()) {
            System.out.println("❌ 판매자 없음: " + storeName);
            return ResponseEntity.status(404).body("판매자를 찾을 수 없습니다: " + storeName);
        }

        Seller seller = sellerOpt.get();
        Long sellerId = seller.getSellerId();
        System.out.println("✅ 조회된 sellerId: " + sellerId);

        try {
            // 2️⃣ 판매자의 상품 목록 조회
            List<Product> products = storeProductService.getProductsBySellerId(sellerId);
            if (products == null || products.isEmpty()) {
                System.out.println("❌ 해당 판매자의 상품 없음: " + storeName);
                return ResponseEntity.status(404).body("해당 판매자의 상품이 없습니다.");
            }

            // 3️⃣ 상품 목록 로그 출력 (디버깅용)
            System.out.println("✅ 조회된 상품 개수: " + products.size());
            products.forEach(product -> {
                System.out.println("✅ 상품 ID: " + product.getProductId());
                System.out.println("✅ 상품 이름: " + product.getName());
                System.out.println("✅ 상품 가격: " + product.getPrice());
            });

            return ResponseEntity.ok(products);

        } catch (Exception e) {
            System.out.println("❌ 상품 목록 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류 발생");
        }
    }
}
