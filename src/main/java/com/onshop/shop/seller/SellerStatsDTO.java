package com.onshop.shop.seller;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SellerStatsDTO {
    private long totalSellers;        // 전체 판매자 수
    private long waitingApproval;     // 인증 대기 중인 판매자 수
    private long approved;            // 인증 완료된 판매자 수
    private long rejected;            // 인증 거절된 판매자 수
}
