package com.onshop.shop.user;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.onshop.shop.address.AddressRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserUpdateRequest {
	
    @JsonAlias("userId") // JSON에서 userId가 String일 경우 Integer로 변환
    private Long userId;
    private String email;
    private String password; // 비밀번호 변경 가능

    // 🔹 전화번호 개수를 유동적으로 받을 수 있도록 변경
    private List<String> phones;

    // 🔹 주소 최대 3개까지 받을 수 있도록 설정
    private List<AddressRequest> addresses;
}
