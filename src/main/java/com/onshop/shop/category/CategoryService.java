package com.onshop.shop.category;

import java.util.List;

public interface CategoryService {
    List<CategoryDTO> getRootCategories();
    List<CategoryDTO> getSubCategories(Long parentId);
    CategoryDTO getCategoryById(Long categoryId);
}
