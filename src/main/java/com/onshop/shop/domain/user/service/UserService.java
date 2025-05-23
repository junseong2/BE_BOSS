package com.onshop.shop.domain.user.service;

import java.util.Map;

import com.onshop.shop.domain.user.dto.ForgetResponseDTO;
import com.onshop.shop.domain.user.dto.UserUpdateRequestDTO;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.enums.UserRole;

public interface UserService {

	void deleteById(Long userId);
	User findById(Long userId);
    User findByEmail(String email);
    User findBySocialId(String socialId);
    Map<String, String> verifyPassword(String password, Long userId);
    void updateUser(User updatedUser);
    void updateUserInfo(UserUpdateRequestDTO request, User user);
    Map<String, String> updatePassword(Map<String, String> request,Long userId);
    void resetPassword(String email, String password);
    String getUserPhones(Long userId);
    User getUserById(Long userId); 
    User saveOrUpdateUser(String socialId, String userName, String userEmail); // 유저 정보 업데이트
    User saveOrUpdateSocialUser(String naverId, String userName, String userEmail, String string, UserRole role); // 소셜 로그인 유저 정보 업데이트
    
    
    ForgetResponseDTO findByUsernameAndPassword(String username, String password);
    
    boolean existsByEmail(String email); 
    
    User registerUser(User user);

    // 판매업자로 상태 업데이트
    void promoteToSellerAndNotify(Long userId, String storeName);
    // 판매업자로 승격 거절
    void rejectSellerAndNotify(Long userId, String storeName);
	
  

}
