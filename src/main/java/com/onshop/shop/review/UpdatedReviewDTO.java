package com.onshop.shop.review;

import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@RequiredArgsConstructor
public class UpdatedReviewDTO {
	private Long reviewId;
	private String reviewText;
	private double ratings;
}
