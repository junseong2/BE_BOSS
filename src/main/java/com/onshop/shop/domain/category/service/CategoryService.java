package com.onshop.shop.domain.category.service;

import java.util.List;

import com.onshop.shop.domain.category.dto.CategoryDTO;
import com.onshop.shop.domain.category.entity.Category;

public interface CategoryService {
    List<CategoryDTO> getRootCategories();
    List<CategoryDTO> getSubCategories(Long parentId);
    CategoryDTO getCategoryById(Long categoryId);
    List<Category> getCategoryHierarchy(); // 대/중분류 전체 조회
    
}
