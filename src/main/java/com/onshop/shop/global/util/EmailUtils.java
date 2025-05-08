package com.onshop.shop.global.util;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailUtils {


    // 유저 체크
    public String userCheck(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        log.info("usercheck:{}", email);
        if(!email.contains("@")){
            return null;
        }

        return email;
    }
    
    // 이메일 인증
    public Boolean emailValidation(String email){
        Pattern pattern = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
        return pattern.matcher(email).matches();
    }

    // 인증 코드 생성
    public String createAuthCode(int maxCodeLength){
        return UUID.randomUUID().toString().substring(0, maxCodeLength);
    }
}
