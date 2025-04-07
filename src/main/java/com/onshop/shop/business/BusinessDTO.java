package com.onshop.shop.business;

import lombok.Getter;
import lombok.Setter;
import java.util.List;

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
