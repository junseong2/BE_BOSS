package com.onshop.shop.exception;

import java.time.LocalDateTime;

import org.springframework.http.HttpStatus;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


@Data
/** 예외처리 메시지 사용자 정의 클래스*/
public class ErrorMessageResponse {
	private final HttpStatus status;
	private final String message;
	private final LocalDateTime timestamp;
	
    public ErrorMessageResponse(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}
