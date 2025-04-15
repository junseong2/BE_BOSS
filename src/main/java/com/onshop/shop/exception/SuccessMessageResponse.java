package com.onshop.shop.exception;

import org.springframework.http.HttpStatus;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SuccessMessageResponse {
	private final HttpStatus status;
	private final String message;
	private final Object data;
}
