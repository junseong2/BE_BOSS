package com.onshop.shop.store;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SellerRepository extends JpaRepository<Seller, Long> {
    Optional<Seller> findByStorename(String storename);  // ✅ Optional<Seller>로 수정
    Seller findBySellerId(Long sellerId);


}
