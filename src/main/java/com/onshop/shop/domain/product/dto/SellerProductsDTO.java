package com.onshop.shop.domain.product.dto;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.onshop.shop.domain.product.enums.DiscountRate;

import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@NoArgsConstructor
public class SellerProductsDTO {
    
    private Long productId;
    private String name;
    private Integer price;
    private String categoryName;
    private String description;
    private Long stock;
    private Long minStock;
    private LocalDateTime expiryDate;
    private Integer discountRate;
    private Integer originPrice;
    private List<String> gImages;
    
	public SellerProductsDTO(Long productId, String name, Integer price, String categoryName, String description,
			Long stock,Long minStock, Timestamp expiryDate, String discountRate, Integer originPrice, String gImage) {

		this.productId = productId;
		this.name = name;
		this.price = price;
		this.categoryName = categoryName;
		this.description = description;
		this.stock = stock;
		this.minStock =minStock;
		this.expiryDate = expiryDate != null ? expiryDate.toLocalDateTime() : null;
		this.discountRate = discountRate != null ? DiscountRate.valueOf(discountRate).getRate(): null;
		this.originPrice = originPrice;
		this.gImages = gImage !=null ? List.of(gImage.split(",")): null; 
		
	}
    

	public SellerProductsDTO(Long productId, String name, Integer price, String categoryName, Long stock) {
	    this.productId = productId;
	    this.name = name;
	    this.price = price;
	    this.categoryName = categoryName;
	    this.stock = stock;
	}

}