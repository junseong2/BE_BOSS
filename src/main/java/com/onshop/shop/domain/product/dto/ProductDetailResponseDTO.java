package com.onshop.shop.domain.product.dto;

import com.onshop.shop.domain.product.entity.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDetailResponseDTO {

    private Long productId;
    private String storename;

    public static ProductDetailResponseDTO fromEntity(Product product) {
        return ProductDetailResponseDTO.builder()
                .productId(product.getProductId())
                .storename(product.getSeller() != null ? product.getSeller().getStorename() : null)
                .build();
    }
}
