package com.onshop.shop.review;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ReviewAnswerRequestDTO {
	private String answerText;
}
