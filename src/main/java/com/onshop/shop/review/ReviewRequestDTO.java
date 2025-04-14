package com.onshop.shop.review;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReviewRequestDTO {
	
	private Long reviewId;
	private Long userId;
	private Long productId;
	private List<String> images;
	private Integer ratings;
	private String reviewText;
}
