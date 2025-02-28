package com.onshop.shop.user;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.persistence.Version;


@Entity
@Table(name = "users") // 테이블명과 일치시킴
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 기존 id 대신 userId를 PK로 설정
    @Column(name = "user_id") // 실제 테이블 컬럼명
    private Long userId; 

    @Column(name = "username", nullable = false, length = 30)
    private String username;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "social_provider")
    private String socialProvider;


    @Column(name = "social_id", length = 50)
    private String socialId;

    @Column(name = "phone1", length = 15)
    private String phone1;

    @Column(name = "phone2", length = 15)
    private String phone2;

    @Column(name = "phone3", length = 15)
    private String phone3;

    @Column(name = "avg_rating", precision = 3, scale = 2)
    private BigDecimal avgRating;
    @Column(name = "role")
    private String role;

    @Column(name = "created_signup", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private java.sql.Timestamp createdSignup;

    
    
    
    
    
    
    
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public User(String socialId, String username, String email) {
        this.socialId = socialId;
        this.username = username;
        this.email = email;
    }

    public User(String username, String email, String password, String role) {
        this.username = username;
        this.email = email;
        this.password = password;
        this.role = role;
    }
  
    public User(long newUserId, String socialId, String userName, String userEmail) {
        this.userId = newUserId;
        this.socialId = socialId;
        this.username = userName;
        this.email = userEmail;
    }

    public User(long newUserId, String socialId, String userName, String userEmail ,String socialProvider) {
        this.userId = newUserId;
        this.socialId = socialId;
        this.username = userName;
        this.email = userEmail;
        this.socialProvider = socialProvider;
    }

}
