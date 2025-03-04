package com.onshop.shop.cart;

import com.onshop.shop.cart.CartEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<CartEntity, Long> {
    List<CartEntity> findByUserId(Long userId); // 여러 개의 CartEntity 반환
    CartEntity findByCartId(Long cartId);
}
