package com.onshop.shop.store;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
            
      //      System.out.println("Response Data: " + response);  // 응답 데이터 로그


            return ResponseEntity.ok(response);
        }

        return ResponseEntity.status(404).body(Map.of("error", "판매자를 찾을 수 없습니다."));
    }
    
    @Autowired
    private ProductsService productsService; // ✅ 올바른 Service 주입



@GetMapping("/users/map")
public ResponseEntity<Map<String, Object>> xxx() {
	
	Map<String, Object> map = new  HashMap<>();
	map.put("a", "AAA");
	map.put("list", Arrays.asList(new User2(100, "홍",LocalDate.now()),new User2(200, "홍",LocalDate.now())));
	
	return ResponseEntity.ok(map);
}
    

    @GetMapping("/product2")
    public ResponseEntity<XXXDTO> getProductsBySeller2(
            @RequestParam Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        try {
        	logger.info("🔍 Received request for sellerId2: " + sellerId);
            
            // sellerId가 null이거나 음수인 경우
           // if (sellerId == null || sellerId <= 0) {
           //     return ResponseEntity.badRequest().body(Map.of("error", "Invalid sellerId"));
         //   }

            Pageable pageable = PageRequest.of(page, size, 
                sort.equals("asc") ? Sort.by("price").ascending() : Sort.by("price").descending());
            
            // 이 부분에서 오류가 발생할 가능성 있음
            Page<Product> productsPage = productsService.getProductsBySeller(sellerId, pageable);
            System.out.println("🔍 Products Page Data: " + productsPage);  // 페이지 데이터 출력

            if (productsPage.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            Map<String, Object> response = new HashMap<>();
            
            response.put("products", productsPage.getContent().get(0));
            response.put("currentPage", productsPage.getNumber());
            response.put("totalItems", productsPage.getTotalElements());
            response.put("totalPages", productsPage.getTotalPages());
            response.put("sortOrder", sort);

           
            

                    
        	logger.info("🔍 Received request for product2: " + productsPage.getContent());

        	
        	
          //  return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            e.printStackTrace(); // 예외 출력
            //return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류 발생", "message", e.getMessage()));
        }
        return null;
    }
    

  @GetMapping("/product")
    public ResponseEntity<Map<String, Object>> getProductsBySeller(
            @RequestParam Long sellerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "8") int size,
            @RequestParam(defaultValue = "asc") String sort
    ) {
        try {
            logger.info("🔍 Received request for sellerId2: " + sellerId);
            
            // sellerId가 null이거나 음수인 경우
            if (sellerId == null || sellerId <= 0) {
                return ResponseEntity.badRequest().body(Map.of("error", "Invalid sellerId"));
            }

            Pageable pageable = PageRequest.of(page, size, 
                sort.equals("asc") ? Sort.by("price").ascending() : Sort.by("price").descending());
            
            // 이 부분에서 오류가 발생할 가능성 있음
            Page<Product> productsPage = productsService.getProductsBySeller(sellerId, pageable);
            System.out.println("🔍 Products Page Data: " + productsPage);  // 페이지 데이터 출력

            if (productsPage.isEmpty()) {
                return ResponseEntity.noContent().build();
            }

            Map<String, Object> response = new HashMap<>();
            response.put("products", productsPage.getContent());
            response.put("currentPage", productsPage.getNumber());
            response.put("totalItems", productsPage.getTotalElements());
            response.put("totalPages", productsPage.getTotalPages());
            response.put("sortOrder", sort);

            
            
            logger.info("📢 Querying products for sellerId: " + sellerId);
            logger.info("📢 Query result: " + (productsPage == null ? "NULL" : productsPage.getTotalElements() + " items found"));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace(); // 예외 출력
            return ResponseEntity.internalServerError().body(Map.of("error", "서버 오류 발생", "message", e.getMessage()));
        }
    }
    
}
