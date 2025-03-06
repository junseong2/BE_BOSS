
package com.onshop.shop.user;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.repository.query.Param;

import com.onshop.shop.user.AddressEntity;
import com.onshop.shop.user.UserEntity;

public interface AddressRepository extends JpaRepository<AddressEntity, Integer> {

    // ✅ 특정 유저의 모든 주소 삭제 (JPQL)
    @Modifying
    @Transactional
    @Query("DELETE FROM AddressEntity a WHERE a.user = :user")
    void deleteAllByUser(@Param("user") UserEntity user);

    // ✅ 특정 유저가 가진 주소 개수 조회
    @Query("SELECT COUNT(a) FROM AddressEntity a WHERE a.user = :user")
    long countByUser(@Param("user") UserEntity user);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM AddressEntity a WHERE a.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Integer userId);

}

