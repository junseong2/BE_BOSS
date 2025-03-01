package com.onshop.shop.user;

import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    @Transactional
    void registerUser(UserEntity user);

    UserEntity findByEmailAndPassword(String email, String password);
    UserEntity findBySocialId(String socialId);
    void updateUser(UserEntity updatedUser);
}
