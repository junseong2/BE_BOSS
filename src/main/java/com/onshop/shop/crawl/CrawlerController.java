package com.onshop.shop.crawl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onshop.shop.category.Category;
import com.onshop.shop.category.CategoryRepository;
import com.onshop.shop.product.Product;
import com.onshop.shop.product.ProductRepository;
import com.onshop.shop.seller.Seller;
import com.onshop.shop.seller.SellerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.security.JwtUtil;
import java.io.File;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/crawl")
@RequiredArgsConstructor
@Slf4j
public class CrawlerController {

    private final AliCrawlerService aliCrawlerService;
    private final TemuCrawlerService temuCrawlerService;
    private final CrawlerService crawlerService;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final JwtUtil jwtUtil;
    
    @CrossOrigin(origins = "*")
    @PostMapping("/product")
    public CrawledProductDto crawlProduct(@RequestBody Map<String, String> body) {
        String url = body.get("url");
        System.out.println("📩 크롤링 요청 받음: " + url);
        if (url.contains("aliexpress.com")) {
            return aliCrawlerService.crawl(url);
        } else if (url.contains("temu.com")) {
            return temuCrawlerService.crawl(url);
        } else {
            throw new IllegalArgumentException("지원하지 않는 쇼핑몰 URL입니다.");
        }
    }

    @GetMapping("/{uuid}/image/{filename}")
    public ResponseEntity<Resource> getImage(@PathVariable String uuid, @PathVariable String filename) {
        try {
            File imageFile = new File("C:/Crawl/" + uuid + "/image/" + filename);
            if (!imageFile.exists()) return ResponseEntity.notFound().build();
            Resource resource = new FileSystemResource(imageFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{uuid}/desc/{filename}")
    public ResponseEntity<Resource> getDescImage(@PathVariable String uuid, @PathVariable String filename) {
        try {
            File imageFile = new File("C:/Crawl/" + uuid + "/desc/" + filename);
            if (!imageFile.exists()) return ResponseEntity.notFound().build();
            Resource resource = new FileSystemResource(imageFile);
            return ResponseEntity.ok()
                    .contentType(MediaType.IMAGE_JPEG)
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    // ✅ 크롤링된 상품 등록
    @PostMapping("/product/upload")
    @Transactional
    public ResponseEntity<?> uploadCrawledProduct(
            @RequestPart("product") String productJson,
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @RequestPart("uuid") String uuid,
            @CookieValue(value = "jwt", required = false) String token
    ) {
        try {
            if (token == null) {
                return ResponseEntity.status(401).body("❌ 권한이 없습니다.");
            }

            Long userId = jwtUtil.extractUserId(token);

            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(productJson);

            Long categoryId = node.get("categoryId").asLong();
            String name = node.get("name").asText();
            String description = node.get("description").asText();
            Integer price = node.get("price").asInt();
            List<String> gImageList = mapper.convertValue(node.get("gImage"), List.class);

            Seller seller = sellerRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("판매자 정보가 없습니다."));

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("카테고리 정보가 없습니다."));

            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .category(category)
                    .seller(seller)
                    .build();

            crawlerService.handleCrawledImages(images, product, uuid);
            productRepository.save(product);

            return ResponseEntity.ok(Map.of("message", "등록 성공"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ 등록 실패: " + e.getMessage());
        }
    }
}

