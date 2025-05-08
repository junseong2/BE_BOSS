package com.onshop.shop.domain.review.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewsDTO {

   private Long reviewId;
   private String username;
   private Integer ratings; 
   private String reviewText;
   private String answerText;
   private String storeName;
   private List<String> imageList;
   private LocalDateTime createdAt;
 

   public ReviewsDTO(Long reviewId, String username, Integer ratings, String reviewText, String images, String storeName, String answerText, Timestamp createdAt) {
       this.reviewId = reviewId;
       this.username = username;
       this.ratings = ratings;
       this.reviewText = reviewText;
       this.imageList = (images != null && !images.isEmpty()) ? Arrays.asList(images.split(",")) : List.of();
       this.storeName= storeName;
       this.answerText = answerText;
       this.createdAt = createdAt.toLocalDateTime();
   }
}