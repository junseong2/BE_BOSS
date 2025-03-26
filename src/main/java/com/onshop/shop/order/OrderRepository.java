package com.onshop.shop.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import io.lettuce.core.dynamic.annotation.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {
	@Query("SELECT o FROM Order o LEFT JOIN FETCH o.user u LEFT JOIN FETCH u.addresses WHERE u.userId = :userId")
	List<Order> findOrdersByUserId(@Param("userId") Long userId);



}
