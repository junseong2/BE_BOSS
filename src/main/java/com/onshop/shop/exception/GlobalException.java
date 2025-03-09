package com.onshop.shop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// 전역 예외처리
@RestControllerAdvice
public class GlobalException {

    // 400 유효성 검사 실패
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorMessageResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {
        ErrorMessageResponse response = new ErrorMessageResponse(HttpStatus.BAD_REQUEST, "유효성 검사 실패 : " + ex.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        return new ResponseEntity<ErrorMessageResponse>(response, HttpStatus.BAD_REQUEST);
    }
    
    // 400 Null 포인터 예외
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorMessageResponse> handleNullPointerException(NullPointerException ex){
    	ErrorMessageResponse response = new ErrorMessageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    	return new ResponseEntity<ErrorMessageResponse>(response, HttpStatus.BAD_REQUEST);
    }
    
    // 400 과잉 요청
    @ExceptionHandler(OverStockException.class)
    public ResponseEntity<ErrorMessageResponse> handleOverStockException(OverStockException ex){
    	ErrorMessageResponse response = new ErrorMessageResponse(HttpStatus.BAD_REQUEST, ex.getMessage());
    	return new ResponseEntity<ErrorMessageResponse>(response, HttpStatus.BAD_REQUEST);
    }
    
    // 404 NOT_FOUND
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorMessageResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorMessageResponse response = new ErrorMessageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<ErrorMessageResponse>(response, HttpStatus.NOT_FOUND);
    }
    
    // 500 INTERNAL_SERVER_ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageResponse> handleAllExceptions(Exception ex) {
    	ErrorMessageResponse response = new ErrorMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "예상치 못한 오류가 발생했습니다");
    	return new ResponseEntity<ErrorMessageResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}