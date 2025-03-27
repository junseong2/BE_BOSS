package com.onshop.shop.seller;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerRequest {

    private Long userId;
    private String storename;
    private String description;
    private String representativeName; // ✅ 대표자 이름
    private String businessRegistrationNumber; // ✅ 사업자등록번호
    private String onlineSalesNumber; // ✅ 통신판매업 신고번호


}
