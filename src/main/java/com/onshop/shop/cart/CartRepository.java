package com.onshop.shop.cart;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Integer userId); // 여러 개의 CartEntity 반환
    Cart findByCartId(Long cartId);
    Cart findByUserIdAndProductId(Integer userId, Long productId);


}
