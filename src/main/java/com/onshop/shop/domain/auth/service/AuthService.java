package com.onshop.shop.domain.auth.service;

import com.onshop.shop.domain.auth.dto.LoginRequestDTO;
import com.onshop.shop.domain.user.dto.EmailVerificationRequestDTO;
import com.onshop.shop.domain.user.dto.ForgetReqeustDTO;
import com.onshop.shop.domain.user.dto.ForgetResponseDTO;
import com.onshop.shop.domain.user.entity.User;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.Cookie;

public interface AuthService {

	
    void registerUser(User user);
    
    Cookie login(LoginRequestDTO loginRequestDTO);
    
    String createNaverLoginRedirectUrl();
    String createKakaoLoginRedirectUrl();
    Cookie naverLoginCallback(String code, String state);
    Cookie kakaoLoginCallback(String code, String state);
    
    /**이메일*/
    MimeMessage createMail(String email, String authCode) throws MessagingException;
    boolean emailVerification(EmailVerificationRequestDTO verificationRequestDTO);
    void sendVerificationCode(String toMail) throws MessagingException;
    void sendAuthCode(String email) throws MessagingException;
    boolean validateMx(String domain);
    
    /** 아이디 찾기*/
    ForgetResponseDTO findUserEmail(ForgetReqeustDTO forgetReqeustDTO);
}
