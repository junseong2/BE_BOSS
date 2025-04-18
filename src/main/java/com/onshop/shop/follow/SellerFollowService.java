package com.onshop.shop.follow;

public interface SellerFollowService {
	
	SellerFollowStateDTO toggleFollow(Long userId, String storeName);

}
