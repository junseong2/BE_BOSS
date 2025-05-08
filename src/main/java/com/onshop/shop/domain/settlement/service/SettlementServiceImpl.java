package com.onshop.shop.domain.settlement.service;

import java.util.List;

import org.springframework.data.domain.Page;
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

/**
 * {@code SettlementServiceImpl}는 판매자와 관리자의 정산 관련 기능을 제공하는 서비스 구현 클래스입니다.
 * 
 * <p><b>주요 기능:</b></p>
 * <ul>
 *     <li>판매자의 정산 요청</li>
 *     <li>판매자의 정산 내역 조회 (검색 조건 포함)</li>
 *     <li>관리자의 정산 상태 조회 및 수정</li>
 *     <li>관리자의 정산 통계 조회</li>
 * </ul>
 * 
 * <p>정산 상태는 {@link SettlementStatus} ENUM을 기반으로 관리됩니다.</p>
 * 
 * @author 사용자
 */
@Service
@RequiredArgsConstructor
public class SettlementServiceImpl implements SettlementService {
	
	private final SettlementRepository settlementRepository;
	private final SellerRepository sellerRepository;
	private final UserRepository userRepository;

	/**
	 * 판매자가 정산을 요청합니다.
	 *
	 * @param requestDTO 정산 요청 정보 (계좌, 금액 등)
	 * @param userId 요청자 ID
	 * @return 요청 결과를 담은 DTO
	 * @throws ResourceNotFoundException 판매자 정보가 없을 경우
	 */
	@Override
	public SettlementsDTO requestSettlement(SettlementRequestDTO requestDTO, Long userId) {
		Seller seller = sellerRepository.findByUserId(userId)
			.orElseThrow(() -> new ResourceNotFoundException("해당 유저의 판매자 계정을 찾을 수 없습니다."));

		Settlement unsaved = Settlement.builder()
				.seller(seller)
				.requestedAmount(requestDTO.getAmount())
				.bankName(requestDTO.getBank())
				.accountNumber(requestDTO.getAccount())
				.accountHolder(requestDTO.getName())
				.status(SettlementStatus.PENDING)
				.build();

		Settlement settlement = settlementRepository.save(unsaved);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new ResourceNotFoundException("해당 유저는 존재하지 않습니다."));

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

	/**
	 * 판매자가 본인의 정산 내역을 조회합니다.
	 *
	 * @param page 페이지 번호
	 * @param size 페이지 크기
	 * @param userId 로그인된 사용자 ID
	 * @param condition 검색 조건 (기간, ID, 사용자명 등)
	 * @return 페이징된 정산 내역 DTO
	 * @throws NotAuthException 판매자 권한이 없을 경우
	 */
	@Override
	public SettlementResponseDTO getSettlements(int page, int size, Long userId, SettlementSearchConditionDTO condition) {
		Pageable pageable = PageRequest.of(page, size);
		
		Seller seller = sellerRepository.findByUserId(userId)
			.orElseThrow(() -> new NotAuthException("요청 권한이 없습니다."));

		Page<SettlementsDTO> settlements = settlementRepository.findAllBySeller(
				seller, 
				condition.getStartDate(), 
				condition.getEndsDate(),
				condition.getSettlementId().orElse(null),
				condition.getUsername(),
				pageable);

		return SettlementResponseDTO.builder()
				.settlements(settlements.toList())
				.totalCount(settlements.getTotalElements())
				.build();
	}

	/**
	 * 관리자 전용: 정산 상태별로 정산 목록을 조회합니다.
	 *
	 * @param status 정산 상태 (PENDING, COMPLETED, REJECTED)
	 * @return 상태별 정산 목록
	 */
	@Override
	public List<Settlement> getAdminSettlementsByStatus(String status) {
	    SettlementStatus settlementStatus = SettlementStatus.valueOf(status.toUpperCase());
	    return settlementRepository.findByStatus(settlementStatus);
	}

	/**
	 * 관리자 전용: 특정 정산 건의 상태를 수정합니다.
	 *
	 * @param settlementId 정산 ID
	 * @param newStatus 변경할 상태
	 * @throws RuntimeException 해당 ID의 정산이 존재하지 않을 경우
	 */
	@Override
	public void updateSettlementStatus(Long settlementId, SettlementStatus newStatus) {
	    Settlement settlement = settlementRepository.findById(settlementId)
	            .orElseThrow(() -> new RuntimeException("정산 내역을 찾을 수 없습니다."));

	    settlement.setStatus(newStatus);
	    settlementRepository.save(settlement);
	}

	/**
	 * 관리자 전용: 전체 정산 통계를 조회합니다.
	 *
	 * @return 정산 건수 통계 DTO
	 */
	@Override
	public SettlementStatsDTO getSettlementStats() {
	    long total = settlementRepository.count();
	    long pending = settlementRepository.countByStatus(SettlementStatus.PENDING);
	    long completed = settlementRepository.countByStatus(SettlementStatus.COMPLETED);
	    long rejected = settlementRepository.countByStatus(SettlementStatus.REJECTED);

	    return new SettlementStatsDTO(total, pending, completed, rejected);
	}
}
