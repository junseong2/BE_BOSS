package com.onshop.shop.domain.follow.service;

import com.onshop.shop.domain.follow.dto.SellerFollowStateDTO;

public interface SellerFollowService {
	
	SellerFollowStateDTO toggleFollow(Long userId, String storeName);

}
