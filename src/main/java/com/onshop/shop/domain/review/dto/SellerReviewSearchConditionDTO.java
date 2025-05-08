package com.onshop.shop.domain.review.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerReviewSearchConditionDTO {
	
	private Integer rating;
	private Boolean isAnswered;
	private String sortby;

}
