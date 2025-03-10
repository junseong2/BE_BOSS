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
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    // OneToMany 관계에서 이미 선언된 주소 필드
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<Address> addresses = new ArrayList<>();

    // 주소 추가 시, 최대 3개까지만 유지하고, 4개 이상일 경우 가장 오래된 주소 삭제
    public void addAddress(Address address) {
        if (addresses.size() >= 3) {
            // 오래된 주소 제거 (리스트에서 첫 번째 주소 제거)
        	addresses.subList(0, addresses.size() - 2).clear();
        }
        addresses.add(address);
    }

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

    // 전화번호를 List로 반환하는 메서드
    public List<String> getPhones() {
        List<String> phones = new ArrayList<>();
        if (phone1 != null) phones.add(phone1);
        if (phone2 != null) phones.add(phone2);
        if (phone3 != null) phones.add(phone3);
        return phones;
    }

    // 전화번호 설정 메서드
    public void setPhones(List<String> phones) {
        if (phones.size() > 0) this.phone1 = phones.get(0);
        else this.phone1 = null;

        if (phones.size() > 1) this.phone2 = phones.get(1);
        else this.phone2 = null;

        if (phones.size() > 2) this.phone3 = phones.get(2);
        else this.phone3 = null;
    }
}
