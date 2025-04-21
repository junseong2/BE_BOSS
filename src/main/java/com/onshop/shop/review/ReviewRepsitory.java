package com.onshop.shop.review;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.onshop.shop.user.User;

import io.lettuce.core.dynamic.annotation.Param;
import io.micrometer.common.lang.Nullable;
public interface ReviewRepsitory extends JpaRepository<Review, Long>  {
	
	
	// 리뷰 평점
	@Query("SELECT new com.onshop.shop.review.AvgRatingResponseDTO("
			+ "AVG(re.rating)) "
			+ "FROM Review re "
			+ "WHERE re.product.productId = :productId ")
	AvgRatingResponseDTO findAvgRatingByProductId(Long productId);
	
	
	// 리뷰 조회 -> 판매자 답변까지 추가된 것
	@Query(value = "SELECT " +
            "r.review_id AS reviewId, " +
            "u.username AS username, " +
            "r.rating AS rating, " +
            "r.review_text AS reviewText, " +
            "r.g_images AS gImages, " +  
            "s.storename AS storeName, " +
            "ra.answer_text AS answerText, " +
            "r.created_at AS createdAt "+
		     "FROM review r " +
		     "LEFT JOIN review_answer ra ON r.review_id = ra.review_id " +
		     "LEFT JOIN users u ON r.user_id = u.user_id " +
		     "LEFT JOIN seller s ON ra.seller_id = s.seller_id " +
		     "WHERE r.product_id = :productId",
		    countQuery = "SELECT COUNT(*) FROM review WHERE product_id = :productId",  nativeQuery = true)
	Page<ReviewsDTO> findByProductId(Long productId, Pageable pageable);

	
	// 리뷰 상세 조회
	boolean existsByProductProductIdAndUserUserId(Long productId, Long UserId);

	// 리뷰 개수
	@Query("SELECT COUNT(re) " 
			+ "FROM Review re "
			+ "WHERE re.product.productId = :productId")
	Long countByProductProductId(Long productId);
	
	
	int deleteByReviewIdAndUser(Long reviewId, User user);
	
	
	/** 판매자*/
	@Query(value = "SELECT " +
            "re.review_id AS reviewId, " +
            "re.rating AS rating, " +
            "p.name AS productName, " +
            "re.review_text AS reviewText, " +
            "u.username AS username, " +
            "re.is_answered AS isAnswered, " +
            "re.created_at AS createdAt, " +
            "re.last_modified_at AS lastModifiedAt, " +
            "ra.answer_id AS answerId, " +
            "ra.answer_text AS answerText, " +
            "s.storename AS storeName, " +
            "ra.created_at AS answerCreatedAt, " +
            "ra.last_modified_at AS answerModifiedAt " +
            "FROM review re " +
            "LEFT JOIN review_answer ra ON ra.review_id = re.review_id " +
            "JOIN product p ON re.product_id = p.product_id " +
            "JOIN users u ON re.user_id = u.user_id " +
            "JOIN seller s ON p.seller_id = s.seller_id " +
            "WHERE p.seller_id = :sellerId " +
            "AND (:rating IS NULL OR re.rating = :rating) " +
            "AND (:isAnswered IS NULL OR re.is_answered = :isAnswered)", 
    countQuery = "SELECT COUNT(re) FROM Review re WHERE re.product.seller.sellerId = :sellerId", 
		    nativeQuery = true)
		Page<SellerReviewsDTO> findAllBySellerId(
		 @Param("sellerId") Long sellerId,
		 @Nullable @Param("rating") Integer rating, 
		 @Nullable @Param("isAnswered") Boolean isAnswered,
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