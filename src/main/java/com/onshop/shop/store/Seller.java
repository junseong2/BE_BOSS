package com.onshop.shop.store;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
@Data
@Entity
@Table(name = "seller")
public class Seller {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long sellerId;  // 판매자 고유 ID

    @Column(name = "user_id", nullable = false)
    private Long userId;  // 사용자 ID (외래 키 참조)

    @Column(name = "storename", unique = true, nullable = false)
    private String storename;  // 상점 이름

    @Column(name = "description")
    private String description;  // 상점 설명

    @Column(name = "created_at", columnDefinition = "DATETIME")
    private LocalDateTime createdAt;  // 생성일

    @Column(name = "updated_at", columnDefinition = "DATETIME")
    private LocalDateTime updatedAt;  // 수정일

    public Seller() {}

    public Seller(Long userId, String storename, String description, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.userId = userId;
        this.storename = storename;
        this.description = description;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

}
