package com.onshop.shop.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.onshop.shop.user.User;

import io.micrometer.common.lang.Nullable;
public interface ReviewRepsitory extends JpaRepository<Review, Long>  {
	
	
	// 리뷰 평점
	@Query("SELECT new com.onshop.shop.review.AvgRatingResponseDTO("
			+ "AVG(re.rating)) "
			+ "FROM Review re "
			+ "WHERE re.product.productId = :productId ")
	AvgRatingResponseDTO findAvgRatingByProductId(Long productId);
	
	
	// 리뷰 조회
	@Query("SELECT new com.onshop.shop.review.ReviewsDTO(" +
		       "re.reviewId, re.user.username, re.rating, re.reviewText, re.gImages, " +
		       "CASE WHEN re.user.userId = :userId THEN true ELSE false END" +
		       ") " +
		       "FROM Review re " +
		       "WHERE re.product.productId = :productId")
	Page<ReviewsDTO> findByProductId(Long productId, Long userId, Pageable pageable);

	
	// 리뷰 상세 조회
	boolean existsByProductProductIdAndUserUserId(Long productId, Long UserId);

	// 리뷰 개수
	@Query("SELECT COUNT(re) " 
			+ "FROM Review re "
			+ "WHERE re.product.productId = :productId")
	Long countByProductProductId(Long productId);
	
	
	int deleteByReviewIdAndUser(Long reviewId, User user);
	
	
	/** 판매자*/
	@Query("SELECT new com.onshop.shop.review.SellerReviewsDTO(" +
		       "re.reviewId, re.rating, re.product.name, re.reviewText, re.user.username, " +
		       "re.isAnswered, re.createdAt, re.lastModifiedAt, " +  // 리뷰 정보
		       "ra.answerId, ra.answerText, ra.seller.storename, ra.createdAt, ra.lastModifiedAt) " +  // ReviewAnswer 정보
		       "FROM Review re LEFT JOIN ReviewAnswer ra ON re.reviewId = ra.review.reviewId " +  
		       "WHERE re.product.seller.sellerId = :sellerId " +
		       "AND (:rating IS NULL OR re.rating = :rating) " +
		       "AND (:isAnswered IS NULL OR re.isAnswered = :isAnswered)")
		Page<SellerReviewsDTO> findAllBySellerId(
		    Long sellerId,
		    @Nullable Integer rating, 
		    @Nullable Boolean isAnswered,
		    Pageable pageable
		);
	// 판매자 리뷰 개수 통계
	@Query("SELECT COUNT(re) FROM Review re "+
		       "WHERE re.product.seller.sellerId = :sellerId " +
		       "AND (:rating IS NULL OR re.rating = :rating) " +
		       "AND (:isAnswered IS NULL OR re.isAnswered = :isAnswered)")
		Long countBySellerId(
		    Long sellerId,
		    @Nullable Integer rating, 
		    @Nullable Boolean isAnswered
		);
}