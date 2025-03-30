package com.onshop.shop.settlement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementRequestDTO {
	
	private Long amount;
	private String bank;
	private String name;
	private Long account;

}
