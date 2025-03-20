package com.onshop.shop.user;

import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

public interface UserService {
    @Transactional
    void registerUser(User user);

    User findByEmailAndPassword(String email, String password);
    User findBySocialId(String socialId);
    void updateUser(User updatedUser);
    User getUserById(Long userId); // ✅ 인터페이스에는 선언만!
    
    
    /**이메일*/
    MimeMessage createMail(String email, String authCode) throws MessagingException;
    boolean emailVerification(EmailVerificationRequestDTO verificationRequestDTO);
    void sendVerificationCode(String toMail) throws MessagingException;
    boolean validateMx(String domain);

}
