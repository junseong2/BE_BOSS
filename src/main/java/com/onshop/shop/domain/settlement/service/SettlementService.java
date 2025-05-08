package com.onshop.shop.domain.settlement.service;

import java.util.List;

import com.onshop.shop.domain.settlement.dto.SettlementRequestDTO;
import com.onshop.shop.domain.settlement.dto.SettlementResponseDTO;
import com.onshop.shop.domain.settlement.dto.SettlementSearchConditionDTO;
import com.onshop.shop.domain.settlement.dto.SettlementStatsDTO;
import com.onshop.shop.domain.settlement.dto.SettlementsDTO;
import com.onshop.shop.domain.settlement.entity.Settlement;
import com.onshop.shop.domain.settlement.enums.SettlementStatus;

public interface SettlementService {
	
	// 정산 요청
	public SettlementsDTO requestSettlement(SettlementRequestDTO requestDTO, Long userId);
	
	// 정산 조회
	public SettlementResponseDTO getSettlements(int page, int size, Long userId, SettlementSearchConditionDTO condition );

	//관리자 정산 조회
	public List<Settlement> getAdminSettlementsByStatus(String status);
	
	// 관리자 정산 업데이트
	void updateSettlementStatus(Long settlementId, SettlementStatus newStatus);
	
	public SettlementStatsDTO getSettlementStats();


	
}
