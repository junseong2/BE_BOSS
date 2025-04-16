package com.onshop.shop.review;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class SellerReviewsDTO {
    private Long reviewId;
    private Integer rating;
    private String productName;
    private String reviewText;
    private String username;
    private Boolean isAnswered;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private Long answerId;
    private String answerText;
    private String storeName;
    private LocalDateTime answerCreatedAt;
    private LocalDateTime answerModifiedAt;

    public SellerReviewsDTO(Long reviewId, Integer rating, String productName, String reviewText, String username,
                            Boolean isAnswered, Timestamp createdAt, Timestamp lastModifiedAt,
                            Long answerId, String answerText, String storeName,
                            Timestamp answerCreatedAt, Timestamp answerModifiedAt) {
    		this.reviewId = reviewId;
    	    this.rating = rating;
    	    this.productName = productName;
    	    this.reviewText = reviewText;
    	    this.username = username;
    	    this.isAnswered = isAnswered;
    	    this.createdAt = createdAt != null ? createdAt.toLocalDateTime() : null;
    	    this.lastModifiedAt = lastModifiedAt != null ? lastModifiedAt.toLocalDateTime() : null;
    	    this.answerId = answerId;
    	    this.answerText = answerText;
    	    this.storeName = storeName;
    	    this.answerCreatedAt = answerCreatedAt != null ? answerCreatedAt.toLocalDateTime() : null;
    	    this.answerModifiedAt = answerModifiedAt != null ? answerModifiedAt.toLocalDateTime() : null;
    }

}



