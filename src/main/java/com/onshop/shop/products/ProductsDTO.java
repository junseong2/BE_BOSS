package com.onshop.shop.products;

import lombok.*;

import java.time.LocalDateTime;
import com.onshop.shop.category.Category;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductsDTO {
	


    private Long productId;
    private Long categoryId;
    private Long sellerId;
    private String name;
    private String description;
    private Integer price;
    private LocalDateTime expiryDate;
    private LocalDateTime createdRegister;

 // ProductsDTO.java
    public static ProductsDTO fromEntity(Product product) {
        return ProductsDTO.builder()
                .productId(product.getProductId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null) // 수정된 부분
                .sellerId(product.getSellerId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .expiryDate(product.getExpiryDate())
                .createdRegister(product.getCreatedRegister())
                .build();
    }


}
