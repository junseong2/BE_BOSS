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

    @Column(name = "header_id", nullable = false)
    private Integer headerId;  // 상단바 ID (디자인 변경 가능)

    @Column(name = "menu_bar_id", nullable = false)
    private Integer menuBarId;  // 메뉴바 ID (메뉴 스타일 변경 가능)

    @Column(name = "navigation_id", nullable = false)
    private Integer navigationId;  // 네비게이션 ID (페이지 이동 방식)

    @Column(name = "seller_menubar_color", length = 7, nullable = true)
    private String sellerMenubarColor;  // ✅ 메뉴바 색상 (예: "#808080")

    @Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;  // 생성일

    @Column(name = "updated_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;  // 수정일

    public Seller() {}

    public Seller(Long sellerId, Long userId, String storename, String description, Integer headerId, Integer menuBarId, Integer navigationId, String sellerMenubarColor, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.sellerId = sellerId;
        this.userId = userId;
        this.storename = storename;
        this.description = description;
        this.headerId = headerId;
        this.menuBarId = menuBarId;
        this.navigationId = navigationId;
        this.sellerMenubarColor = sellerMenubarColor; // ✅ 추가
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    // ✅ Getter 및 Setter 추가
    public String getSellerMenubarColor() {
        return sellerMenubarColor;
    }

    public void setSellerMenubarColor(String sellerMenubarColor) {
        this.sellerMenubarColor = sellerMenubarColor;
    }
}
