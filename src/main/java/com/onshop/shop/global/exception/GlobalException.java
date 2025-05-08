package com.onshop.shop.global.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.slf4j.Slf4j;



/**
 *** CustomException 사용 설명서
 * 하단에 CustomException 보이시죠? 해당 커스텀 예외를 기반으로 모든 예외를 처리할 수 있습니다.
 *  @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorMessageResponse> handleCustomException(CustomException ex) {
    	ErrorMessageResponse response = new ErrorMessageResponse(ex.getStatus(), ex.getMessage());
    	return new ResponseEntity<ErrorMessageResponse>(response, ex.getStatus());
    }
    
 * 예를 들어, 아래와 같이 BadRequestException 클래스를 만듭니다.
 * public class BadRequestException extends CustomException {
	private static final long serialVersionUID = 1L;

	public BadRequestException(String message) {
		 super(message, HttpStatus.BAD_REQUEST);
    }
}
 
 * 이를 아래와 같이 특정 클래스 내에서 throw new BadRequestException("잘못된 형식의 MX 코드 입니다."); 형식으로
 * 예외를 던지면, GlobalException 에서 해당 예외를 잡아서 자동으로 응답을 클라이언트에게 보내줍니다. 
 */

// 전역 예외처리
@RestControllerAdvice
@Slf4j
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
    
    // 토큰 만료 예외   
    @ExceptionHandler(ExpiredJwtException.class)
    public ResponseEntity<ErrorMessageResponse> handleJwtExpiredException(ExpiredJwtException ex) {
        ErrorMessageResponse response = new ErrorMessageResponse(HttpStatus.NOT_FOUND, ex.getMessage());
        return new ResponseEntity<ErrorMessageResponse>(response, HttpStatus.UNAUTHORIZED);
    }
    
    
    /** 커스텀 예외처리 */
    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ErrorMessageResponse> handleCustomException(CustomException ex) {
    	ErrorMessageResponse response = new ErrorMessageResponse(ex.getStatus(), ex.getMessage());
    	return new ResponseEntity<ErrorMessageResponse>(response, ex.getStatus());
    }
    
    // 500 INTERNAL_SERVER_ERROR
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorMessageResponse> handleAllExceptions(Exception ex) {
    	log.error("전역 예외:{}", ex.getMessage());
    	ErrorMessageResponse response = new ErrorMessageResponse(HttpStatus.INTERNAL_SERVER_ERROR, "서버 측 문제입니다. 서버 로그를 확인하세요.");
    	return new ResponseEntity<ErrorMessageResponse>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}