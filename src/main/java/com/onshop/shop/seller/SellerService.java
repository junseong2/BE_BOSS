package com.onshop.shop.seller;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onshop.shop.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
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
    public Seller findBysellerId(Long sellerId) {
        return sellerRepository.findBySellerId(sellerId);
    }

    // 판매자 정보 저장
    public Seller save(Seller seller) {
        return sellerRepository.save(seller);
    }
    
    // 판매업 등록
    public Seller registerSeller(Long userId, String storename, String description,  String RepresentativeName, String businessRegistrationNumber, String onlineSalesNumber) {
        Seller newSeller = new Seller();
        newSeller.setUserId(userId);
        newSeller.setStorename(storename);
        newSeller.setDescription(description);
        newSeller.setRepresentativeName(RepresentativeName);
        newSeller.setBusinessRegistrationNumber(businessRegistrationNumber);
        newSeller.setOnlineSalesNumber(onlineSalesNumber);
        newSeller.setRegistrationStatus("등록 대기");  // 초기 상태는 '등록 대기'
        newSeller.setApplicationDate(LocalDateTime.now());  // 신청 날짜 설정
        return sellerRepository.save(newSeller);  // 저장
    }
    
    // 이미 `userId`로 등록된 판매자가 있는지 확인
    public boolean isUserAlreadySeller(Long userId) {
        return sellerRepository.findByUserId(userId).isPresent();
    }
    
    // 관리자 판매업 등록 승인 및 거절
    public void approveSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("판매자 정보를 찾을 수 없습니다."));

        seller.setRegistrationStatus("등록 완료");
        sellerRepository.save(seller);

//        userService.promoteToSellerAndNotify(seller.getUserId(), seller.getStorename());
    }
    
    public void rejectSeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("판매자 정보를 찾을 수 없습니다."));

        seller.setRegistrationStatus("등록 거절");
        sellerRepository.save(seller);

//        userService.promoteToSellerAndNotify(seller.getUserId(), seller.getStorename());
    }
    
    public List<Seller> getAllSellers(){
    	return sellerRepository.findAll();
    }
    
    // seller중복가입 방지
    public boolean isUserSeller(Long userId) {
        return sellerRepository.findByUserId(userId).isPresent();
    }

    
    public SellerStatsDTO getSellerStats() {
        // 각 상태별 판매자 수를 계산
        long totalSellers = sellerRepository.count();  // 전체 판매자 수
        long waitingApproval = sellerRepository.countByRegistrationStatus("등록 대기");
        long approved = sellerRepository.countByRegistrationStatus("등록 완료");
        long rejected = sellerRepository.countByRegistrationStatus("등록 거절");

        // DTO에 값 설정하여 반환
        return new SellerStatsDTO(totalSellers, waitingApproval, approved, rejected);
    }
    
    
    /** 홈페이지*/
    
    public List<SellerStoresDTO> getAllStores(int page, int size) {
    	Pageable pageable = PageRequest.of(page, size);
    	List<Seller> storeEntries = sellerRepository.findAll(pageable).toList();
    	
    	
    	if(storeEntries.isEmpty()) {
    		throw new ResourceNotFoundException("조회할 판매자가 없습니다.");
    	}
    	
    	return storeEntries.stream().map((sellerEntity)->{
    		log.info("sellerEntity{}",sellerEntity);
    		return SellerStoresDTO.builder()
    				.storeName(sellerEntity.getStorename())
    				.description(sellerEntity.getDescription())
    				.logoUrl(sellerEntity.getSettings())
    				.build();
    	}).toList();
    			
    
    	
    }
    
    
    
    
    
    
    
}
