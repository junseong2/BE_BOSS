package com.onshop.shop.store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

// UIElement.java 엔티티
@Entity
public class UIElement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String sellerId;
    private String type;
    
    @Column(columnDefinition = "JSON")
    private String data;  // JSON 형식 저장
    
    private int sortOrder; // 순서 관리
}// UIElement.java 엔티티
