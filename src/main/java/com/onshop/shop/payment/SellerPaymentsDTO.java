package com.onshop.shop.payment;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SellerPaymentsDTO {
	
	private String impUid;
	private String username;
	private Object paidDate;
	private Object paymentMethod;
	private Object totalAmount;
	private Object paymentStatus;

}
