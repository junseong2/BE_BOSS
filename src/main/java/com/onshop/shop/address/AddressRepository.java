package com.onshop.shop.address;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.user.User;

public interface AddressRepository extends JpaRepository<Address, Long> {

    @Transactional
    void deleteByUser(User user);

    @Transactional
    void deleteByUserUserId(Long userId);

    long countByUser(User user);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Address a WHERE a.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
    
    Optional<Address> findByUser_UserIdAndIsDefaultTrue(Long userId);
    
    List<Address> findByUserUserId(Long userId);
    
    //주소 중복확인용
    boolean existsByUserAndAddress1AndPost(User user, String address1, String post);
    
    List<Address> findByUser(User user);
    
    // ✅ 특정 주소를 가져오기 (중복인데 기본 주소로 설정하려고 할 때 사용)
    Address findByUserAndAddress1AndPost(User user, String address1, String post);
    
}
