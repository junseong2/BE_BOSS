package com.onshop.shop.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ForgetReqeustDTO {
	private String username;
	private String password;

}
