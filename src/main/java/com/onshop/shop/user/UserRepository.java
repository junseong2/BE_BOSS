
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
    

   
    Optional<User> findByEmailAndPassword(String email, String password);//미래에 아이디비번찾기용도
}

