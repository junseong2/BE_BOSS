package com.onshop.shop.domain.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SettlementStatsDTO {
    private long totalSettlements;   // 전체 정산 요청 수
    private long pending;            // 대기 상태
    private long completed;          // 완료 상태
    private long rejected;           // 거절 상태
}
