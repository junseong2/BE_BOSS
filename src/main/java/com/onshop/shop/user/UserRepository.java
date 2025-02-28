package com.onshop.shop.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository  // ✅ 추가!
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findBySocialId(String socialId); // socialId로 조회
    
    @Query("SELECT MAX(u.userId) FROM User u") // 가장 큰 user_id 가져오기
    Optional<Long> findMaxUserId();
}
