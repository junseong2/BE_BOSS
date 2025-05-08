package com.onshop.shop.global.exception;

import org.springframework.http.HttpStatus;

public class DeleteFailureException extends CustomException {

	public DeleteFailureException(String message) {
		super(message, HttpStatus.CONFLICT);
		
	}

}
