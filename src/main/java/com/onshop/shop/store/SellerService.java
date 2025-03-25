package com.onshop.shop.store;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
    
    public Optional<Seller> getSellerByUserId(Long userId) {
        return sellerRepository.findByUserId(userId);
    }
    public Optional<Seller> getSellerById(Long sellerId) {
        return sellerRepository.findById(sellerId);
    }
 
    
    


    // Seller의 settings을 업데이트하는 메서드 추가
    public Seller updateSellerSettings(Long sellerId, String settings) {
        Optional<Seller> optionalSeller = sellerRepository.findById(sellerId);
        if (optionalSeller.isPresent()) {
            Seller seller = optionalSeller.get();
            seller.setSettings(settings); // settings 값을 업데이트
            System.out.println("my settings:"+settings);
            return sellerRepository.save(seller); // DB에 저장
        } else {
            throw new RuntimeException("Seller not found with ID: " + sellerId); // 예외 처리
        }
    }
    
    
    // Seller의 settings을 업데이트하는 메서드 추가
    public Seller updateSellerMobilesettings(Long sellerId, String settings) {
        Optional<Seller> optionalSeller = sellerRepository.findById(sellerId);
        if (optionalSeller.isPresent()) {
            Seller seller = optionalSeller.get();
            seller.setMobilesettings(settings); // settings 값을 업데이트
            return sellerRepository.save(seller); // DB에 저장
        } else {
            throw new RuntimeException("Seller not found with ID: " + sellerId); // 예외 처리
        }
    }
    

    public Seller updateSellerAllSettings(Long sellerId, List<Map<String, Object>> settings) {
        // settings를 순회하며 저장하는 로직 추가
        for (Map<String, Object> setting : settings) {
            // 개별 설정 저장 로직
            String type = (String) setting.get("type");
            System.out.println("설정 업데이트 중: " + type);
            // 타입별 저장 처리
        }
        return sellerRepository.findById(sellerId).orElseThrow(() -> new RuntimeException("Seller not found"));
    }

   
    public Seller updateSellerAllMobileSettings(Long sellerId, List<Map<String, Object>> mobileSettings) {
        for (Map<String, Object> setting : mobileSettings) {
            String type = (String) setting.get("type");
            System.out.println("모바일 설정 업데이트 중: " + type);
        }
        return sellerRepository.findById(sellerId).orElseThrow(() -> new RuntimeException("Seller not found"));
    }

    
    public void updateSettings(Long sellerId, List<UIElementDTO> elements) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("판매자를 찾을 수 없음: " + sellerId));
        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            // ✅ DTO 리스트 → JSON 배열 변환
            String json = objectMapper.writeValueAsString(elements);
            seller.setSettings(json);
            sellerRepository.save(seller);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 변환 실패: " + e.getMessage());
        }
    }
    
 // SellerService.java
    public List<UIElementDTO> getSettings(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("판매자를 찾을 수 없음: " + sellerId));
        
        if (seller.getSettings() == null) return List.of();
        
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(
                seller.getSettings(),
                new TypeReference<List<UIElementDTO>>(){}
            );
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패: " + e.getMessage());
        }
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
