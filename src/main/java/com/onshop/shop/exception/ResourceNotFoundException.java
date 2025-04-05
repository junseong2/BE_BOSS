package com.onshop.shop.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends CustomException {

	private static final long serialVersionUID = 1L;

	public ResourceNotFoundException(String message) {
		super(message, HttpStatus.NOT_FOUND);
 
	}
}
