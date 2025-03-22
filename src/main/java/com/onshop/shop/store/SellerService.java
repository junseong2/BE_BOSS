package com.onshop.shop.store;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    public Optional<Seller> getSellerByStorename(String storeName) {
        Optional<Seller> seller = sellerRepository.findByStorename(storeName);

        if (seller.isEmpty()) {
            throw new RuntimeException("판매자를 찾을 수 없습니다: " + storeName);
        }

        return seller;
    }
    
    
    
    
    
    
    // 배경색 업데이트
    public Seller updateHeaderBackgroundColor(Long sellerId, String backgroundColor) {
        // Seller 객체 찾기
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("판매자를 찾을 수 없습니다."));

        try {
            // 기존 settings JSON 값을 가져옴
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> settings = objectMapper.readValue(seller.getSettings(), Map.class);
            
            // 배경색을 settings에 추가
            settings.put("backgroundColor", backgroundColor);
            
            // 수정된 settings를 다시 JSON 문자열로 저장
            seller.setSettings(objectMapper.writeValueAsString(settings));

            // Seller 업데이트
            return sellerRepository.save(seller);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("배경색 업데이트 실패");
        }
    }
    
    
    
    // 판매자 ID로 판매자 조회
    public Seller findBysellerId(Long id) {
        return sellerRepository.findBySellerId(id);
    }

    // 판매자 정보 저장
    public Seller save(Seller seller) {
        return sellerRepository.save(seller);
    }

    
    
    
    
    
    
    
    
    
    
    
    
    
}
