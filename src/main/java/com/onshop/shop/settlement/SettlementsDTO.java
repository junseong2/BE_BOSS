package com.onshop.shop.settlement;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementsDTO {
	
	private Long settlementId;
	private Object requestDate;
	private Object settleDate; 
	private SettlementStatus status;
	private String bank;
	private String name;
	private Object accountNum;
	private Long totalAmount;
	

}
