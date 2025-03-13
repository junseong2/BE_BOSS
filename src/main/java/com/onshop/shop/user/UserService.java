package com.onshop.shop.user;

import org.springframework.transaction.annotation.Transactional;

public interface UserService {
    @Transactional
    void registerUser(User user);

    User findByEmailAndPassword(String email, String password);
    User findBySocialId(String socialId);
    void updateUser(User updatedUser);
    User getUserById(Integer userId); // ✅ 인터페이스에는 선언만!

}
