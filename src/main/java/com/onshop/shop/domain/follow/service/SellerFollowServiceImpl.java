package com.onshop.shop.domain.follow.service;

import org.springframework.stereotype.Service;

import com.onshop.shop.domain.follow.dto.SellerFollowStateDTO;
import com.onshop.shop.domain.follow.entity.SellerFollow;
import com.onshop.shop.domain.follow.repository.SellerFollowRepository;
import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.repository.UserRepository;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * 판매자 팔로우 기능을 처리하는 서비스 구현체입니다.
 * 
 * 사용자는 특정 판매자를 팔로우하거나 팔로우를 취소할 수 있으며,
 * 해당 상태를 토글 방식으로 변경할 수 있습니다.
 */
@Service
@RequiredArgsConstructor
public class SellerFollowServiceImpl implements SellerFollowService {

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final SellerFollowRepository sellerFollowRepository;

    /**
     * 사용자가 특정 판매자를 팔로우하거나 언팔로우합니다.
     * 
     * <p>
     * - 이미 팔로우 상태라면 언팔로우 처리하고 {@code isFollow = false} 반환<br>
     * - 팔로우 상태가 아니라면 새로 팔로우하고 {@code isFollow = true} 반환
     * </p>
     *
     * @param userId    팔로우 요청을 한 사용자 ID
     * @param storeName 판매자의 상점 이름
     * @return 현재 팔로우 상태를 나타내는 {@link SellerFollowStateDTO}
     * @throws ResourceNotFoundException 유저 또는 판매자를 찾을 수 없는 경우
     */
    @Override
    public SellerFollowStateDTO toggleFollow(Long userId, String storeName) {

        // 사용자 조회 (없으면 예외)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(userId + "로 식별되는 유저가 존재하지 않습니다."));

        // 판매자 조회 (없으면 예외)
        Seller seller = sellerRepository.findByStorename(storeName)
                .orElseThrow(() -> new ResourceNotFoundException(storeName + "으로 등록된 판매자가 존재하지 않습니다."));

        // 현재 팔로우 상태 확인
        Boolean isFollow = sellerFollowRepository.existsBySellerAndUser(seller, user);

        SellerFollow followEntity = null;

        // 이미 팔로우 중이면 언팔로우 처리
        if (isFollow) {
            sellerFollowRepository.deleteBySellerAndUser(seller, user);
            isFollow = false;
        } else {
            // 팔로우 상태가 아니면 팔로우 생성
            followEntity = sellerFollowRepository.save(
                SellerFollow.builder()
                    .seller(seller)
                    .user(user)
                    .build()
            );
            isFollow = true;
        }

        // 팔로우 ID는 팔로우 상태일 때만 포함 (UI에서 추후에 사용할 수 있도록 유지)
        return SellerFollowStateDTO.builder()
                .followId(followEntity != null ? followEntity.getFollowId() : null)
                .isFollow(isFollow)
                .build();
    }

}
