package com.onshop.shop.settlement;

public interface SettlementService {
	
	// 정산 요청
	public SettlementsDTO requestSettlement(SettlementRequestDTO requestDTO, Long userId);
	
	// 정산 조회
	public SettlementResponseDTO getSettlements(int page, int size, Long userId, SettlementSearchConditionDTO condition );
	
	public void updateSellerStatus(Long sellerId, String status);
	
	SettlementResponseDTO getAllSettlements(int page, int size, SettlementSearchConditionDTO condition);
}
