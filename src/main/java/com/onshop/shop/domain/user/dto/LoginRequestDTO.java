
package com.onshop.shop.domain.user.dto; // 패키지명은 프로젝트 구조에 맞게 변경

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequestDTO {
    private String email;
    private String password;
}

