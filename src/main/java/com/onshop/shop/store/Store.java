package com.onshop.shop.store;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class Store {

    @Id
    @GeneratedValue
    private Long id;

    @JsonProperty("store_name")
    private String storeName;

    @JsonProperty("address")
    private String address;

    private double latitude;  // 위도
    private double longitude; // 경도

    
    private double distance; // StoreService에서 거리순정렬때만 사용할 거리 필드

    
    public Store(String storeName, String address, double latitude, double longitude) {
        this.storeName = storeName;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    // 기본 생성자
    public Store() {}
}
