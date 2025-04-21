	package com.onshop.shop.product;
	
	import lombok.*;
	
	import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

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
	    private List<String> gImage;
	    private LocalDateTime expiryDate;
	    private LocalDateTime createdRegister;
	    private String storename;
	   
	 // ProductsDTO.java
	    public static ProductsDTO fromEntity(Product product) {
	    	List<String> imageUrls = product.getImageList().stream()
//	                .map(imageName -> "http://localhost:5000/uploads/" + imageName) // URL 변환
	                .map(imageName ->  "https://bossassets.s3.amazonaws.com/"  + imageName) // URL 변환
	                .collect(Collectors.toList());
	
	        return ProductsDTO.builder()
	                .productId(product.getProductId())
	                .categoryId(product.getCategory() != null ? product.getCategory().getId() : null) // 수정된 부분
	                .name(product.getName())
	                .description(product.getDescription())
	                .price(product.getPrice())
	                .gImage(imageUrls)
	                .expiryDate(product.getExpiryDate())
	                .createdRegister(product.getCreatedRegister())
	                .build();
	    }
	
	
	}
