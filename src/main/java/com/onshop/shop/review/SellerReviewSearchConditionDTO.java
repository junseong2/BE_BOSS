package com.onshop.shop.review;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerReviewSearchConditionDTO {
	
	private Integer rating;
	private Boolean isAnswered;
	private String sortby;

}
