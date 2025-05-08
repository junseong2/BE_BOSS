package com.onshop.shop.domain.seller.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerRequestDTO {

    private Long userId;
    private String storename;
    private String description;
    private String representativeName; // ✅ 대표자 이름
    private String businessRegistrationNumber; // ✅ 사업자등록번호
    private String onlineSalesNumber; // ✅ 통신판매업 신고번호


}
