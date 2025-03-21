package com.onshop.shop.store;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onshop.shop.product.Product;

import jakarta.transaction.Transactional;

@Service
public class StoreProductService {

    private final StoreProductRepository storeProductRepository;

    public StoreProductService(StoreProductRepository storeProductRepository) {
        this.storeProductRepository = storeProductRepository;
    }

    // 수정된 상품 조회 메소드
    

    @Transactional
    public List<Product> getProductsBySellerId(Long sellerId) {
        try {
            List<Product> products = storeProductRepository.findBySeller_SellerId(sellerId);  // Seller와 Product의 관계를 찾는 쿼리
            System.out.println("✅ 상품 목록 조회 성공: " + products.size() + "개 상품 반환");
            return products;
        } catch (Exception e) {
            System.out.println("❌ 상품 목록 조회 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("상품 목록 조회 중 오류 발생", e);
        }
    }
}
