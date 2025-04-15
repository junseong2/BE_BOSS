
package com.onshop.shop.user;

import java.util.List;

import com.onshop.shop.address.AddressDTO;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserDTO {
    private String username;
    private String email;
    private String password;
    private String phone1;
    private String phone2;
    private String phone3;
    private String role;
    
    // 주소 정보 리스트
    private List<AddressDTO> addresses;
}

