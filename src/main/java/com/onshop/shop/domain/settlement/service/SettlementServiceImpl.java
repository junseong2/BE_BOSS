package com.onshop.shop.domain.settlement.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.domain.settlement.dto.SettlementRequestDTO;
import com.onshop.shop.domain.settlement.dto.SettlementResponseDTO;
import com.onshop.shop.domain.settlement.dto.SettlementSearchConditionDTO;
import com.onshop.shop.domain.settlement.dto.SettlementStatsDTO;
import com.onshop.shop.domain.settlement.dto.SettlementsDTO;
import com.onshop.shop.domain.settlement.entity.Settlement;
import com.onshop.shop.domain.settlement.enums.SettlementStatus;
import com.onshop.shop.domain.settlement.repository.SettlementRepository;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.repository.UserRepository;
import com.onshop.shop.global.exception.NotAuthException;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {
	
	private final SettlementRepository settlementRepository;
	private final SellerRepository sellerRepository;
	private final UserRepository userRepository;

	
	// 정산 요청
	@Override
	public SettlementsDTO requestSettlement(SettlementRequestDTO requestDTO, Long userId) {
		
		Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()-> new ResourceNotFoundException("해당 유저의 판매자 계정을 찾을 수 없습니다."));
		
		Settlement unsaved = Settlement.builder()
				.seller(seller)
				.requestedAmount(requestDTO.getAmount())
				.bankName(requestDTO.getBank())
				.accountNumber(requestDTO.getAccount())
				.accountHolder(requestDTO.getName())
				.status(SettlementStatus.PENDING)
				.build();
		
		Settlement settlement = settlementRepository.save(unsaved);
		
		
		User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("해당 유저는 존재하지 않습니다."));
		
		
		return SettlementsDTO.builder()
				.settlementId(settlement.getSettlementId())
				.status(settlement.getStatus())
				.accountNum(requestDTO.getAccount())
				.bank(requestDTO.getBank())
				.name(user.getUsername())
				.totalAmount(settlement.getRequestedAmount())
				.requestDate(settlement.getCreatedDate())
				.settleDate(settlement.getCreatedDate().plusMonths(1).withDayOfMonth(15))
				.build();
	}

	
	// 정산 내역 조회
	@Override
	public SettlementResponseDTO getSettlements(int page, int size, Long userId, SettlementSearchConditionDTO condition) {
		Pageable pageable = (Pageable) PageRequest.of(page, size);
		
		Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()-> new NotAuthException("요청 권한이 없습니다."));
		
		
		// 정산 내역
		List<SettlementsDTO> settlements = settlementRepository.findAllBySeller(
				seller, 
				condition.getStartDate(), 
				condition.getEndsDate(),
				condition.getSettlementId().orElse(null),
				condition.getUsername(),
				pageable).toList();
		
		// 전체 목록 수
		Long totalCount = settlementRepository.countBySeller(
				seller, 
				condition.getStartDate(), 
				condition.getEndsDate(),
				condition.getSettlementId().orElse(null),
				condition.getUsername());
		
		return SettlementResponseDTO.builder()
				.settlements(settlements)
				.totalCount(totalCount)
				.build();
	}

	// 관리자 정산 조회
	@Override
	public List<Settlement> getAdminSettlementsByStatus(String status) {
	    SettlementStatus settlementStatus = SettlementStatus.valueOf(status.toUpperCase());
	    return settlementRepository.findByStatus(settlementStatus);
	}
	
	// 관리자 정산 업데이트 
	@Override
	public void updateSettlementStatus(Long settlementId, SettlementStatus newStatus) {
	    Settlement settlement = settlementRepository.findById(settlementId)
	            .orElseThrow(() -> new RuntimeException("정산 내역을 찾을 수 없습니다."));

	    settlement.setStatus(newStatus);
	    settlementRepository.save(settlement);
	}
	
	// 관리자 정산 인원 기록
	@Override
	public SettlementStatsDTO getSettlementStats() {
	    long total = settlementRepository.count();
	    long pending = settlementRepository.countByStatus(SettlementStatus.PENDING);
	    long completed = settlementRepository.countByStatus(SettlementStatus.COMPLETED);
	    long rejected = settlementRepository.countByStatus(SettlementStatus.REJECTED);

	    return new SettlementStatsDTO(total, pending, completed, rejected);
	}


}
