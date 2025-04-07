package com.onshop.shop.settlement;

import java.time.LocalDateTime;
import java.util.Optional;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SettlementSearchConditionDTO {
	
	private LocalDateTime startDate;
	private LocalDateTime endsDate;
	private String username;
	private Optional<Long> settlementId;

}
