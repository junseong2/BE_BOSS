package com.onshop.shop.payment;

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
