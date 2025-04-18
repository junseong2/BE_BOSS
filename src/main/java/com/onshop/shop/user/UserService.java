package com.onshop.shop.user;

import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;


public interface UserService {
    @Transactional
    void registerUser(User user);

    User findByEmail(String email);
    User findBySocialId(String socialId);
    void updateUser(User updatedUser);
    User getUserById(Long userId); // ✅ 인터페이스에는 선언만!

    
    /**이메일*/
    MimeMessage createMail(String email, String authCode) throws MessagingException;
    boolean emailVerification(EmailVerificationRequestDTO verificationRequestDTO);
    void sendVerificationCode(String toMail) throws MessagingException;
    void sendAuthCode(String email) throws MessagingException;
    boolean validateMx(String domain);
    
    /** 아이디 찾기*/
    ForgetResponseDTO findUserEmail(ForgetReqeustDTO forgetReqeustDTO);
    
    // 판매업자로 상태 업데이트
    void promoteToSellerAndNotify(Long userId, String storeName);
    // 판매업자로 승격 거절
    void rejectSellerAndNotify(Long userId, String storeName);

    

}
