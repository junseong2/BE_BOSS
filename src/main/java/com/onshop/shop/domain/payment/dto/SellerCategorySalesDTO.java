package com.onshop.shop.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


// 카테고리 별 총 매출 액
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerCategorySalesDTO {
	private String categoryName;
	private Long totalSales;
}
