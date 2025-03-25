package com.onshop.shop.UI.header;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onshop.shop.store.Seller;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "headers") // MySQL 예약어 충돌 방지를 위해 "header" 대신 "headers" 사용
public class Header {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long headerId; // 일반적인 네이밍 규칙 적용

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false) 
    @OnDelete(action = OnDeleteAction.CASCADE) // Seller 삭제 시 관련 헤더 삭제
    private Seller seller;
    
    private String name; // 헤더 이름 (예: "메인 헤더", "서브 헤더")
    private String backgroundColor; // 헤더의 배경 색상
}
