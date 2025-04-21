package com.onshop.shop.review;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface ReviewService {
	
	ReviewResponseDTO getReviews(Long productId, int page, int size);
    void createReview(ReviewRequestDTO reviewDto);
    void updateReview(ReviewRequestDTO reviewDto);
    void deleteReview(Long reviewId, Long userId);
    
    AvgRatingResponseDTO getAvgRating(Long productId);
    
    void registerReviewImages(List<MultipartFile> images, Review review); // 리뷰 이미지 업로드
    
    
    /** 판매자 */
    // 판매자 리뷰 조회
    SellerReviewResponseDTO getSellerReviews(int page, int size,  Long userId, SellerReviewSearchConditionDTO condition);
    
    // 판매자 리뷰 추가
    void createSellerReview(SellerAnswerRequestDTO answerDTO, Long userId, Long reviewId);
    
    // 판매자 리뷰 수정
    void updateSellerReview(SellerAnswerRequestDTO answerDTO, Long userId, Long reviewId);
    
    // 판매자 리뷰 삭제
    void deleteSellerReview(Long answerId, Long userId,Long reviewId);

}
