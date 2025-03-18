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

    // 판매자 정보 반환
    @GetMapping("/store/{storeName}/products/info")
    public ResponseEntity<?> getSellerByStorename(@PathVariable String storeName) {
        System.out.println("✅ 요청된 storename: " + storeName); // 콘솔 출력 (디버깅용)
        
        Optional<Seller> seller = sellerService.getSellerByStorename(storeName);

        if (seller.isEmpty()) {
            System.out.println("❌ 판매자 없음: " + storeName); // 판매자가 없는 경우 로그 출력
            return ResponseEntity.status(404).body("판매자를 찾을 수 없습니다: " + storeName);
        }

        System.out.println("✅ 판매자 조회 성공: " + seller.get()); // 성공한 경우 로그 출력
        return ResponseEntity.ok(seller.get());
    }

    // 상품 목록 반환
    @GetMapping("/store/{storeName}/products")
    public ResponseEntity<?> getProductsByStore(@PathVariable String storeName) {
        System.out.println("✅ 상품 조회 요청 storename: " + storeName); // 콘솔 출력 (디버깅용)
        
        Optional<Seller> seller = sellerService.getSellerByStorename(storeName);

        if (seller.isEmpty()) {
            System.out.println("❌ 판매자 없음: " + storeName); // 판매자가 없는 경우 로그 출력
            return ResponseEntity.status(404).body("판매자를 찾을 수 없습니다: " + storeName);
        }

        Long sellerId = seller.get().getSellerId();
        System.out.println("✅ 조회된 sellerId: " + sellerId); // sellerId 출력

        try {
            List<Product> products = storeProductService.getProductsBySellerId(sellerId);
            System.out.println("✅ 조회된 상품 개수: " + products.size()); // 상품 개수 출력

            // 상품 정보 출력
            for (Product product : products) {
                System.out.println("✅ 상품 ID: " + product.getProductId());  // 상품 ID 출력
                System.out.println("✅ 상품 이름: " + product.getName());      // 상품 이름 출력
                // 추가로 원하는 다른 속성들도 출력 가능
                System.out.println("✅ 상품 가격: " + product.getPrice());     // 상품 가격 출력
            }

            if (products.isEmpty()) {
                System.out.println("❌ 해당 판매자의 상품 없음: " + storeName); // 상품이 없는 경우 로그 출력
                return ResponseEntity.status(404).body("해당 판매자의 상품이 없습니다.");
            }

            return ResponseEntity.ok(products);
        } catch (Exception e) {
            System.out.println("❌ 상품 목록 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body("서버 오류 발생");
        }

    }
}
