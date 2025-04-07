package com.onshop.shop.orderDetail;

import com.onshop.shop.order.Order;
import com.onshop.shop.product.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Data;

// 추가 이유: 상품, 주문 간의 연관관계가 없어서 이후 주문관리, 결제내역, 정산관리 시 판매자별 처리에 어려움을 경험하여 두 테이블 간에 다대다 관계가 필요하여 추가함
@Entity
@Data
@Builder
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderItemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private int quantity;     // 수량
    private int price;        // 각 상품 가격
    private int totalPrice;   // 총 금액 (가격 * 수량)

    // getters and setters
}