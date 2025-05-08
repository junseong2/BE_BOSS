package com.onshop.shop.domain.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerPaymentStatisticsDTO {
	
	private Long totalPrice;
	private Long totalOrderCount;
	private Long paidOrderCount;
	private Long canceledTotalPrice;

}
