package com.onshop.shop.seller;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByStorename(String storename);  // ✅ Optional<Seller>로 수정
    Seller findBySellerId(Long sellerId);
    Optional<Seller>  findByUserId(Long userId);


    void deleteByUserId(Long userId);

    
    long countByRegistrationStatus(String registrationStatus);

    // 전체 판매자 수
    long count();
    
  
}
