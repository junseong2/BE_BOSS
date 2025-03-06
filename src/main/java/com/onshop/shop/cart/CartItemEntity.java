package com.onshop.shop.cart;

import jakarta.persistence.*;
@Entity
@Table(name = "cart_items")
public class CartItemEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cartItemId;

    @ManyToOne
    @JoinColumn(name = "cart_id", nullable = false) // nullable = false 추가
    private CartEntity cart;

    private Long productId;
    private Integer quantity;

    // 기본 생성자
    public CartItemEntity() {
    }

    // Getters and Setters
    public Long getCartItemId() {
        return cartItemId;
    }
    public void setCartItemId(Long cartItemId) {
        this.cartItemId = cartItemId;
    }
    public CartEntity getCart() {
        return cart;
    }
    public void setCart(CartEntity cart) {
        this.cart = cart;
    }
    public Long getProductId() {
        return productId;
    }
    public void setProductId(Long productId) {
        this.productId = productId;
    }
    public Integer getQuantity() {
        return quantity;
    }
    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    @Override
    public String toString() {
        return "CartItemEntity {cartItemId=" + cartItemId +
               ", productId=" + productId +
               ", quantity=" + quantity + "}";
    }
}
