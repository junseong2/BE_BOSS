package com.onshop.shop.domain.business.dto;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BusinessDTO {
    private List<Business> businesses;  // 사업자 등록번호 목록

    @Getter
    @Setter
    public static class Business {
        private String b_no;  // 사업자 등록번호
    }
}
