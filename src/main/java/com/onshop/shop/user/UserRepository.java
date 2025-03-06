package com.onshop.shop.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.onshop.shop.user.UserEntity;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Integer> { // ✅ User → UserEntity 변경
    Optional<UserEntity> findBySocialId(String socialId); 

    @Query("SELECT MAX(u.userId) FROM UserEntity u") // ✅ User → UserEntity 변경
    Optional<Integer> findMaxUserId();
    //Optional<Integer> findByUserId();//1 2 3 4와 같은 userId로 검색
    Optional<UserEntity> findByEmailAndPassword(String email, String password);
}
