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
}
