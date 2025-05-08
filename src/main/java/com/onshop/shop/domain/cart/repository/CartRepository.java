package com.onshop.shop.domain.cart.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onshop.shop.domain.cart.entity.Cart;
import com.onshop.shop.domain.product.entity.Product;

public interface CartRepository extends JpaRepository<Cart, Long> {
    List<Cart> findByUserId(Long userId); // 여러 개의 CartEntity 반환
    Cart findByCartId(Long cartId);
    Cart findByUserIdAndProduct(Long userId, Product product);
    
    
//    판매자 장바구니 아이템 개수
    Long countByUserId(Long userId);
    


}
