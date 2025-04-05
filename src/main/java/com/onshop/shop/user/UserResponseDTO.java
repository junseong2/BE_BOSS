

package com.onshop.shop.user;

import lombok.Data;

@Data
public class UserResponseDTO {
    private Long userId;
    private String username;
    private String email;
    // 주소 정보가 필요하다면 Address DTO 또는 간단한 필드들 추가
    // 단, 순환 참조를 방지할 수 있도록 필요한 정보만 담아야 함.

    // Getter, Setter
}
