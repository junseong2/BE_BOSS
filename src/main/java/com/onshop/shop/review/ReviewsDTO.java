package com.onshop.shop.review;

import java.util.Arrays;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ReviewsDTO {

   private Long reviewId;
   private String username;
   private Integer ratings; 
   private String reviewText;
   private List<String> imageList;
   private Boolean isMine;

   public ReviewsDTO(Long reviewId, String username, Integer ratings, String reviewText, String images, Boolean isMine) {
       this.reviewId = reviewId;
       this.username = username;
       this.ratings = ratings;
       this.reviewText = reviewText;
       this.imageList = (images != null && !images.isEmpty()) ? Arrays.asList(images.split(",")) : List.of();
       this.isMine = isMine;
   }
}