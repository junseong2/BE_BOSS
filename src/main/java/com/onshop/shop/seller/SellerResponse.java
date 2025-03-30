package com.onshop.shop.seller;

import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerResponse {
    private Long id;
    private String storename;
    private String representativeName;
    private String businessRegistrationNumber;
    private String registrationStatus;
    private LocalDate applicationDate;

    public static SellerResponse from(Seller seller) {
        SellerResponse dto = new SellerResponse();
        dto.id = seller.getSellerId();
        dto.storename = seller.getStorename();
        dto.representativeName = seller.getRepresentativeName();
        dto.businessRegistrationNumber = seller.getBusinessRegistrationNumber();
        dto.registrationStatus = seller.getRegistrationStatus();
        dto.applicationDate = seller.getApplicationDate().toLocalDate();
        return dto;
    }
}
