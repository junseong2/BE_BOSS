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
	private String bank; // 은행이름
	private String name; // 예금주
	private String account; // 계좌번호

}
