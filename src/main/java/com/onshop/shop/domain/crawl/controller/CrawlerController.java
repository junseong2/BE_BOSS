package com.onshop.shop.domain.crawl.controller;

import java.io.File;
import java.util.List;
import java.util.Map;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onshop.shop.domain.category.entity.Category;
import com.onshop.shop.domain.category.repository.CategoryRepository;
import com.onshop.shop.domain.crawl.dto.CrawledProductDTO;
import com.onshop.shop.domain.crawl.service.AliCrawlerServiceImpl;
import com.onshop.shop.domain.crawl.service.CrawlerService;
import com.onshop.shop.domain.crawl.service.TemuCrawlerService;
import com.onshop.shop.domain.inventory.entity.Inventory;
import com.onshop.shop.domain.inventory.repository.InventoryRepository;
import com.onshop.shop.domain.product.entity.Product;
import com.onshop.shop.domain.product.enums.DiscountRate;
import com.onshop.shop.domain.product.repository.ProductRepository;
import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.global.util.JwtUtil;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/crawl")
@RequiredArgsConstructor
@Slf4j
public class CrawlerController {

    private final AliCrawlerServiceImpl aliCrawlerService;
    private final TemuCrawlerService temuCrawlerService;
    private final CrawlerService crawlerService;
    private final ProductRepository productRepository;
    private final SellerRepository sellerRepository;
    private final CategoryRepository categoryRepository;
    private final InventoryRepository inventoryRepository;
    private final JwtUtil jwtUtil;
    
    @CrossOrigin(origins = "*")
    @PostMapping("/product")
    public CrawledProductDTO crawlProduct(@RequestBody Map<String, String> body) {
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

    // 크롤링된 상품 등록
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
            Integer originPrice = node.get("originPrice").asInt();
            Long minStock = node.get("minStock").asLong();
            Long stock = node.get("stock").asLong();
            int discountRate = node.get("discountRate").asInt();
            
            List<String> gImageList = mapper.convertValue(node.get("gImage"), List.class);

            Seller seller = sellerRepository.findByUserId(userId)
                    .orElseThrow(() -> new RuntimeException("판매자 정보가 없습니다."));

            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new RuntimeException("카테고리 정보가 없습니다."));

            Product product = Product.builder()
                    .name(name)
                    .description(description)
                    .price(price)
                    .originPrice(originPrice)
                    .discountRate(DiscountRate.fromRate(discountRate))
                    .seller(seller)
                    .category(category)
                    .isDiscount(discountRate >0 ? true : false) // 할인율이 0보다 크면 할인 적용
                    .build();

            crawlerService.handleCrawledImages(images, product, uuid);
            Product savedProduct = productRepository.save(product);
            

       
            inventoryRepository.save(
                    Inventory.builder()
                    .product(savedProduct)
                    .seller(seller)
                    .minStock(minStock)
                    .stock(stock)
                    .build()
            		);

            return ResponseEntity.ok(Map.of("message", "등록 성공"));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().body("❌ 등록 실패: " + e.getMessage());
        }
    }
}

