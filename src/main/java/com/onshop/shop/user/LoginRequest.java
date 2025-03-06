
package com.onshop.shop.user; // 패키지명은 프로젝트 구조에 맞게 변경

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {
    private String email;
    private String password;
}

