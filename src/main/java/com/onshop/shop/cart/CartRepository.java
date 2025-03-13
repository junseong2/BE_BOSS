package com.onshop.shop.cart;

import com.onshop.shop.cart.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Integer userId); // 여러 개의 CartEntity 반환
    Cart findByCartId(Long cartId);
    Cart findByUserIdAndProductId(Integer userId, Long productId);


}
