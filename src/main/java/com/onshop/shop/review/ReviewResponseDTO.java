package com.onshop.shop.review;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewResponseDTO {
	private List<ReviewsDTO> reviews;
	private Long totalCount;
	private double avgRating;
}
