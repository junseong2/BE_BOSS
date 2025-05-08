package com.onshop.shop.domain.review.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.domain.review.dto.AvgRatingResponseDTO;
import com.onshop.shop.domain.review.dto.ReviewRequestDTO;
import com.onshop.shop.domain.review.dto.ReviewResponseDTO;
import com.onshop.shop.domain.review.dto.SellerAnswerRequestDTO;
import com.onshop.shop.domain.review.dto.SellerReviewResponseDTO;
import com.onshop.shop.domain.review.dto.SellerReviewSearchConditionDTO;
import com.onshop.shop.domain.review.entity.Review;

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
