package com.onshop.shop.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> { // ✅ User → UserEntity 변경
    Optional<UserEntity> findBySocialId(String socialId); 

    @Query("SELECT MAX(u.userId) FROM UserEntity u") // ✅ User → UserEntity 변경
    Optional<Long> findMaxUserId();
    Optional<UserEntity> findByEmailAndPassword(String email, String password);
}
