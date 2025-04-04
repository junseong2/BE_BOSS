
package com.onshop.shop.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.onshop.shop.user.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	
	Optional<User> findByUserId(Long userId); 
	Optional<User> findBySocialId(String socialId); 

    @Query("SELECT MAX(u.userId) FROM User u") 
    Optional<Long> findMaxUserId();
    


   

    Optional<User> findByEmail(String email);//미래에 아이디비번찾기용도
    
    boolean existsByEmail(String email); // 해당 이메일을 가진 유저가 존재하는지 체크

    
    // 유저 이메일 찾기
    ForgetResponseDTO findByUsernameAndPassword(String username, String password);
    
    //사용자 상태 조회
    Optional<User> findByRole(String role);
}
