package com.onshop.shop.follow;

import org.springframework.stereotype.Service;

import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.seller.Seller;
import com.onshop.shop.seller.SellerRepository;
import com.onshop.shop.user.User;
import com.onshop.shop.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerFollowServiceImpl implements SellerFollowService {
	
	private final UserRepository userRepository;
	private final SellerRepository sellerRepository;
	private final SellerFollowRepository sellerFollowRepository;
	
	// 팔로우 상태 토글
	@Override
	public SellerFollowStateDTO toggleFollow(Long userId, String storeName) {
		
		User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException(userId +"로 식별되는 유저가 존재하지 않습니다."));
		Seller seller = sellerRepository.findByStorename(storeName).orElseThrow(()-> new ResourceNotFoundException(storeName +"으로 등록된 판매자가 존재하지 않습니다."));
		 
		
		Boolean isFollow = sellerFollowRepository.existsBySellerAndUser(seller, user); // 팔로우 상태 반환
		
		
		SellerFollow followEntity = null;
		
		// 팔로우 상태라면 취소
		if(isFollow) {
			sellerFollowRepository.deleteBySellerAndUser(seller, user);
			isFollow = false; // 팔로우 상태를 비활성화로 변경
		} else {
			// 팔로우 아니라면 팔로우 요청
			followEntity = sellerFollowRepository.save(
					SellerFollow.builder()
					.seller(seller)
					.user(user)
					.build()
					);
			isFollow = true;
		}
		
		
		return SellerFollowStateDTO.builder()
				.followId(followEntity !=null ? followEntity.getFollowId() : null) // 필요 없을 것 같긴 한데 일단은 둔다.
				.isFollow(isFollow)
				.build();
	}

}
