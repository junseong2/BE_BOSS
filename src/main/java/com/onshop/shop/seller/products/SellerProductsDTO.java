package com.onshop.shop.seller.products;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor // ✅ 롬복으로 전체 생성자 추가 가능
public class SellerProductsDTO {
    
    private Long productId;
    private String name;
    private Integer price;
    private String description;
    private String categoryName;
    private Long stock;

 
   
}
