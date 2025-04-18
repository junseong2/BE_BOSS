package com.onshop.shop.product;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
