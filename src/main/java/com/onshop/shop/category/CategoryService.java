package com.onshop.shop.category;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    List<CategoryDTO> getRootCategories();
    List<CategoryDTO> getSubCategories(Long parentId);
    CategoryDTO getCategoryById(Long categoryId);
    List<Category> getCategoryHierarchy(); // 대/중분류 전체 조회
    
}
