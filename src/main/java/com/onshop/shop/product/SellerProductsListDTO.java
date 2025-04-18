package com.onshop.shop.product;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SellerProductsListDTO {
    private Long productId;
    private String name;
    private Integer price;
    private Integer originPrice;
    private String categoryName;
    private String description;
    private LocalDateTime expiryDate;
    private Boolean isDiscount;
    private DiscountRate discountRate;
    private Long stock;
    private String gImage;

    // ✅ 이 생성자 하나만 남겨주세요!
    public SellerProductsListDTO(Long productId, String name, Integer price, Integer originPrice,
                                  String categoryName, String description, Timestamp expiryDate,
                                  Boolean isDiscount, String discountRate, Long stock, String gImage) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.originPrice = originPrice;
        this.categoryName = categoryName;
        this.description = description;
        this.expiryDate = expiryDate != null ? expiryDate.toLocalDateTime() : null;
        this.isDiscount = isDiscount;
        this.discountRate = discountRate != null ? DiscountRate.valueOf(discountRate) : null;
        this.stock = stock;
        this.gImage = gImage;
    }
}
