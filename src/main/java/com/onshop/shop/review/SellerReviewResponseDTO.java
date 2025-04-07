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
public class SellerReviewResponseDTO {
	private List<SellerReviewsDTO> reviews;
	private Long totalCount;

}
