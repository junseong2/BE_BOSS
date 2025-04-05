package com.onshop.shop.user;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailVerificationRequestDTO {
	
	@NotEmpty(message="이메일은 필수입니다.")
	private String email;
	@Size(min = 5, max=5, message="인증번호는 5자 이어야 합니다.")
	private String code;

}
