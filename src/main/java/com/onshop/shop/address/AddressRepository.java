package com.onshop.shop.address;

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
}
