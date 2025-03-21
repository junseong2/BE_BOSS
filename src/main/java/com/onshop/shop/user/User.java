package com.onshop.shop.user;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id") // DB 컬럼명과 명시적 매핑
    private Integer userId;//int형이 성능 절약된다고 함!

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


    @JsonIgnore // ✅ LazyInitializationException 방지 (이 필드가 JSON 응답에서 제외됨)
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private List<Address> addresses = new ArrayList<>();



    public List<String> getPhones() {
        List<String> phones = new ArrayList<>();
        if (phone1 != null) {
			phones.add(phone1);
		}
        if (phone2 != null) {
			phones.add(phone2);
		}
        if (phone3 != null) {
			phones.add(phone3);
		}
        return phones;
    }


    public void setPhones(List<String> phones) {
        if (phones.size() > 0) {
			this.phone1 = phones.get(0);
		} else {
			this.phone1 = null;
		}

        if (phones.size() > 1) {
			this.phone2 = phones.get(1);
		} else {
			this.phone2 = null;
		}

        if (phones.size() > 2) {
			this.phone3 = phones.get(2);
		} else {
			this.phone3 = null;
		}
    }

}
