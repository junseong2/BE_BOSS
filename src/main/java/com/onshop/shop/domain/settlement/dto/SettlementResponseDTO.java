package com.onshop.shop.domain.settlement.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SettlementResponseDTO {
	private List<SettlementsDTO> settlements;
	private Long totalCount;

}
