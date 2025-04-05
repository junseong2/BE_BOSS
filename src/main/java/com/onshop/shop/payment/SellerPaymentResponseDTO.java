package com.onshop.shop.payment;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerPaymentResponseDTO {
	
	private List<SellerPaymentsDTO> payments;
	private Long totalCount;

}
