package com.onshop.shop.store;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StoreProductDTO {
    private Long id;
    private String name;
    private String description;
    private int price;
    private String imageUrl;
    private Long categoryId;  // FK 매핑
    private Long storeId;  // ✅ Store 엔티티 대신 storeId만 포함
}
