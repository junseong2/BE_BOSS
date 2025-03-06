package com.onshop.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

// 전역 예외처리
@RestControllerAdvice
public class GlobalException extends ResponseEntityExceptionHandler {
	
	
	// 404 NOT_FOUND
	@ExceptionHandler(exception = ResourceNotFoundException.class)
	public ResponseEntity<?> handleResourceNotFound(ResourceNotFoundException ex){
		ErrorMessageResponse resoponse = new ErrorMessageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
		return new ResponseEntity<>(resoponse, HttpStatus.NOT_FOUND); 
	}
}
