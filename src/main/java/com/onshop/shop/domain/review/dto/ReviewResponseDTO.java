package com.onshop.shop.domain.review.dto;

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
