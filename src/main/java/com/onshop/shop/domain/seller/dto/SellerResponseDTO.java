package com.onshop.shop.domain.seller.dto;

import java.time.LocalDate;

import com.onshop.shop.domain.seller.entity.Seller;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SellerResponseDTO {
    private Long id;
    private String storename;
    private String representativeName;
    private String businessRegistrationNumber;
    private String registrationStatus;
    private LocalDate applicationDate;

    public static SellerResponseDTO from(Seller seller) {
        SellerResponseDTO dto = new SellerResponseDTO();
        dto.id = seller.getSellerId();
        dto.storename = seller.getStorename();
        dto.representativeName = seller.getRepresentativeName();
        dto.businessRegistrationNumber = seller.getBusinessRegistrationNumber();
        dto.registrationStatus = seller.getRegistrationStatus();
        dto.applicationDate = seller.getApplicationDate().toLocalDate();
        return dto;
    }
}
