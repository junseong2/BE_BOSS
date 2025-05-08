	package com.onshop.shop.domain.product.dto;
	
	import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
	public class ProductsDTO {
		
	    private Long productId;
	    private Long categoryId;
	    private Long sellerId;
	    private String name;
	    private String description;
	    private Integer price;
	    private List<String> gImage;
	    private LocalDateTime expiryDate;
	    private LocalDateTime createdRegister;
	    private String storename;
	    
	    
	    public ProductsDTO(Long productId, Long categoryId, Long sellerId, String name,
                String description, Integer price, String gImage,
                LocalDateTime expiryDate, LocalDateTime createdRegister, String storename) {

				 this.productId = productId;
				 this.categoryId = categoryId;
				 this.sellerId = sellerId;
				 this.name = name;
				 this.description = description;
				 this.price = price;
				 this.gImage = (gImage != null && !gImage.isEmpty())
				         ? List.of(gImage.split(",")).stream()
				             .map(String::trim)
				             .map(img -> "https://bossassets.s3.amazonaws.com/" + img)
				             .collect(Collectors.toList())
				         : List.of();
				 this.expiryDate = expiryDate;
				 this.createdRegister = createdRegister;
				 this.storename = storename;
	    }
				   
	    public static ProductsDTO fromEntity(Product product) {
	        List<String> imageUrls = product.getImageList().stream()
	            .collect(Collectors.toList());

	        return ProductsDTO.builder()
	            .productId(product.getProductId())
	            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null) // null 체크
	            .sellerId(product.getSeller() != null ? product.getSeller().getSellerId() : null) // null 체크
	            .name(product.getName())
	            .description(product.getDescription())
	            .price(product.getPrice())
	            .gImage(imageUrls)
	            .expiryDate(product.getExpiryDate())
	            .createdRegister(product.getCreatedRegister())
	            .storename(product.getSeller() != null ? product.getSeller().getStorename() : null) // storename도 null 체크
	            .build();
	    }

	
	
	}
