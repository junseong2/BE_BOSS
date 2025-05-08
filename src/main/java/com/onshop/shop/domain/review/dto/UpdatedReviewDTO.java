package com.onshop.shop.domain.review.dto;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UpdatedReviewDTO {
	private Long reviewId;
	private String reviewText;
	private double ratings;
}
