package com.onshop.shop.domain.category.dto;

import java.util.List;

import com.onshop.shop.domain.category.entity.Category;

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
