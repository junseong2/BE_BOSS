package com.onshop.shop.domain.seller.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.onshop.shop.domain.seller.entity.Seller;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByStorename(String storename);  // Optional<Seller>로 수정
    Seller findBySellerId(Long sellerId);
    Optional<Seller>  findByUserId(Long userId);


    void deleteByUserId(Long userId);

    
    long countByRegistrationStatus(String registrationStatus);

    // 전체 판매자 수
    long count();
    
  
}
