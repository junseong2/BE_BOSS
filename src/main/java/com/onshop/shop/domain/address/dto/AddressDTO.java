
package com.onshop.shop.domain.address.dto;

import com.onshop.shop.domain.address.entity.Address;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressDTO {
    private String address1;
    private String address2;
    private String post;
    private Boolean isDefault;
    
    public static AddressDTO fromEntity(Address address) {
        AddressDTO dto = new AddressDTO();
        dto.setAddress1(address.getAddress1());
        dto.setAddress2(address.getAddress2());
        dto.setPost(address.getPost());
        dto.setIsDefault(address.getIsDefault());
        return dto;
}
}
