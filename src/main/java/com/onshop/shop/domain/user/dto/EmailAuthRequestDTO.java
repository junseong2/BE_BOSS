package com.onshop.shop.domain.user.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class EmailAuthRequestDTO {
	
    @NotEmpty(message = "이메일은 필수입니다.")
	private String email;
}
