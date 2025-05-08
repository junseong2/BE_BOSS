package com.onshop.shop.domain.follow.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onshop.shop.domain.follow.entity.SellerFollow;
import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.user.entity.User;

public interface SellerFollowRepository extends JpaRepository<SellerFollow, Long> {
	
	Boolean existsBySellerAndUser(Seller seller, User user); // 팔로우 유무 확인
	
	
	int deleteBySellerAndUser(Seller seller, User user); // 팔로우 취소
	
	
//	Long countBySeller(Seller seller, User user); // 판매자별 팔로우 개수 
	
	

}
