package com.onshop.shop.follow;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onshop.shop.seller.Seller;
import com.onshop.shop.user.User;

public interface SellerFollowRepository extends JpaRepository<SellerFollow, Long> {
	
	Boolean existsBySellerAndUser(Seller seller, User user); // 팔로우 유무 확인
	
	
	int deleteBySellerAndUser(Seller seller, User user); // 팔로우 취소
	
	
//	Long countBySeller(Seller seller, User user); // 판매자별 팔로우 개수 
	
	

}
