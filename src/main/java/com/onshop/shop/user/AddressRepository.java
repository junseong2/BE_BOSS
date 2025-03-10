package com.onshop.shop.user;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

public interface AddressRepository extends JpaRepository<Address, Integer> {

    @Transactional
    void deleteByUser(User user);

    @Transactional
    void deleteByUserUserId(Integer userId);

    long countByUser(User user);
    
    @Modifying
    @Transactional
    @Query("DELETE FROM Address a WHERE a.user.userId = :userId")
    void deleteAllByUserId(@Param("userId") Integer userId);
    List<Address> findByUser_UserId(Integer userId);  // ✅ userId를 정확히 참조


}
