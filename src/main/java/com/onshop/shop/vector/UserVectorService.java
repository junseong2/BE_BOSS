package com.onshop.shop.vector;

import java.util.List;

public interface UserVectorService {
    void handleAddToCartAndUpdateUserVector(Long userId, Long productId);
    
    /**
     * ✅ 유저 기반 상품 추천
     *
     * @param userId 추천 대상 사용자 ID
     * @param n 추천할 상품 개수 (기본 20)
     * @param m 유사 사용자 수 (기본 3)
     * @return 추천된 productId 리스트
     */
    List<Long> recommendProducts(Long userId, int n, int m);
}
