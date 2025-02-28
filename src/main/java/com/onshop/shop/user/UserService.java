package com.onshop.shop.user;

import com.onshop.shop.user.UserEntity;
import org.springframework.transaction.annotation.Transactional;

public interface UserService { // ✅ 인터페이스로 변경
    @Transactional
    void registerUser(UserEntity user);
    UserEntity findByEmailAndPassword(String email, String password);
}
