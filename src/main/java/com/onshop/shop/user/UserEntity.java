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
    @Column(name = "user_id") // DB 컬럼명과 명시적 매핑
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
    
    
 // ✅ 전화번호를 리스트로 변환하는 메서드 추가
    public List<String> getPhones() {
        List<String> phones = new ArrayList<>();
        if (phone1 != null) phones.add(phone1);
        if (phone2 != null) phones.add(phone2);
        if (phone3 != null) phones.add(phone3);
        return phones;
    }

    // ✅ 전화번호 리스트를 개별 컬럼에 매핑하는 메서드 추가
    public void setPhones(List<String> phones) {
        this.phone1 = phones.size() > 0 ? phones.get(0) : null;
        this.phone2 = phones.size() > 1 ? phones.get(1) : null;
        this.phone3 = phones.size() > 2 ? phones.get(2) : null;
    }
    
}
