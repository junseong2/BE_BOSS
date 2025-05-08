package com.onshop.shop.global.exception;

import org.springframework.http.HttpStatus;

public class NotAuthException extends CustomException {

	public NotAuthException(String message) {
		super(message, HttpStatus.FORBIDDEN);
	}

}
