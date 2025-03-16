package com.onshop.shop.products;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
public class ProductsDTO {

    private Long productId;
    private Long categoryId;
    private String categoryName; // 카테고리 이름 추가
    private Long sellerId;
    private String name;
    private String description;
    private Integer price;
    private List<String> gImage;
    private LocalDateTime expiryDate;
    private LocalDateTime createdRegister;

    // Entity에서 DTO로 변환하는 메소드
    public static ProductsDTO fromEntity(Product product) {
        List<String> imageUrls = product.getImageList().stream()
                .map(imageName -> "http://localhost:5000/uploads/" + imageName) // URL 변환
                .collect(Collectors.toList());

        // DTO에 categoryName 추가
        return ProductsDTO.builder()
                .productId(product.getProductId())
                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null) // 카테고리 ID 설정
                .categoryName(product.getCategory() != null ? product.getCategory().getName() : null) // 카테고리 이름 설정
                .sellerId(product.getSellerId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .gImage(imageUrls)
                .expiryDate(product.getExpiryDate())
                .createdRegister(product.getCreatedRegister())
                .build();
    }
}