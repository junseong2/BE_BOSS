package com.onshop.shop.user;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userId;

    private String username;
    private String email;
    private String password;
    private String socialProvider;
    private String socialId;
    private String phone1;
    private String phone2;
    private String phone3;
    private BigDecimal avgRating;
    private String role;

    @CreationTimestamp
    private Timestamp createdSignup;

    // ✅ AddressEntity를 사용하도록 수정
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AddressEntity> addresses = new ArrayList<>();
}
