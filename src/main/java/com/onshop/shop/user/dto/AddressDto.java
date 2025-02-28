package com.onshop.shop.user.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDto {
    private String address1;
    private String address2;
    private String post;
    private Boolean isDefault;
}
