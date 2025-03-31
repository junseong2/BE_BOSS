package com.onshop.shop.settlement;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.onshop.shop.exception.NotAuthException;
import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.seller.Seller;
import com.onshop.shop.seller.SellerRepository;
import com.onshop.shop.user.User;
import com.onshop.shop.user.UserRepository;

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
				.status(SettlementStatus.PENDING)
				.build();
		
		Settlement settlement = settlementRepository.save(unsaved);
		
		
		User user = userRepository.findById(userId).orElseThrow(()-> new ResourceNotFoundException("해당 유저는 존재하지 않습니다."));
		
		
		return SettlementsDTO.builder()
				.settlementId(settlement.getSettlementId())
				.status(settlement.getStatus())
				.accountNum((long) 1122070398)
				.bank("부산")
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

}
