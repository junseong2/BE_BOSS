package com.onshop.shop.review;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerReviewsDTO {
    
    private Long reviewId;
    private Integer rating;
    private String productName;
    private String reviewText;
    private String username;
    private Boolean isAnswered;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private SellerReviewAnswerDTO reviewAnswer; // DTO 사용

    // 명시적인 생성자 추가
    public SellerReviewsDTO(Long reviewId, Integer rating, String productName, String reviewText,
                            String username, Boolean isAnswered, LocalDateTime createdAt, LocalDateTime lastModifiedAt,
                            Long answerId, String answerText, String storeName, LocalDateTime answerCreatedAt, LocalDateTime answerModifiedAt) {
        this.reviewId = reviewId;
        this.rating = rating;
        this.productName = productName;
        this.reviewText = reviewText;
        this.username = username;
        this.isAnswered = isAnswered;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        this.reviewAnswer = (answerId != null) ? new SellerReviewAnswerDTO(answerId, reviewId, answerText, storeName, answerCreatedAt, lastModifiedAt) : null;
    }
}
