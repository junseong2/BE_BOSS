package com.onshop.shop.category;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CategoryHierarchyDTO {
	
	private Long id;
	private String name;
	
	private List<Category> subCategories;
	

}
