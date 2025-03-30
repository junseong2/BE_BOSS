package com.onshop.shop.settlement;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.onshop.shop.seller.Seller;

import io.lettuce.core.dynamic.annotation.Param;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
	
    /** 
     * 페이징을 적용한 정산 목록 조회 
     */
    @Query("SELECT new com.onshop.shop.settlement.SettlementsDTO("
            + " st.settlementId, "
            + " st.createdDate, "
            + " st.updatedDate, "
            + " st.status, "
            + " '우리은행', " // TODO: 하드코딩된 은행 이름(향후 사용자가 등록한 입출금 은행명으로 변경)
            + " u.username, " // TODO: User 엔티티에서 username 가져옴(향후 사용자가 지정한 예금주명으로 변경)
            + " '1234567866', " // TODO: 하드코딩된 계좌번호(향후 사용자가 등록한 입출금 계좌로 변경)
            + " st.requestedAmount) "
            + "FROM Settlement st "
            + "JOIN Seller s ON st.seller.sellerId = s.sellerId "
            + "JOIN User u ON s.userId = u.userId "
            + "WHERE s = :seller "
            + "AND st.createdDate BETWEEN :startDate AND :endDate "
            + "AND (:settlementId IS NULL OR st.settlementId = :settlementId) "
            + "AND (:username IS NULL OR u.username LIKE %:username%)")
    Page<SettlementsDTO> findAllBySeller(
        @Param("seller") Seller seller, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate,
        @Param("settlementId") Long settlementId,  // 정산번호로 정산내역 검색 
        @Param("username") String username,        // username(예금주명)으로 정산내역 검색 
        Pageable pageable // 페이징 적용
    );

    /** 
     * 전체 정산 개수 조회 
     */
    @Query("SELECT COUNT(st) "
            + "FROM Settlement st "
            + "JOIN Seller s ON st.seller.sellerId = s.sellerId "
            + "JOIN User u ON s.userId = u.userId "
            + "WHERE s = :seller "
            + "AND st.createdDate BETWEEN :startDate AND :endDate "
            + "AND (:settlementId IS NULL OR st.settlementId = :settlementId) "
            + "AND (:username IS NULL OR u.username LIKE %:username%)")
    Long countBySeller(
        @Param("seller") Seller seller, 
        @Param("startDate") LocalDateTime startDate, 
        @Param("endDate") LocalDateTime endDate,
        @Param("settlementId") Long settlementId,  
        @Param("username") String username
    );
}
