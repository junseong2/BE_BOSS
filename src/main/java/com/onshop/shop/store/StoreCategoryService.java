package com.onshop.shop.store;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onshop.shop.category.Category;

@Service
public class StoreCategoryService {

    private final StoreCategoryRepository categoryRepository;

    public StoreCategoryService(StoreCategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<Category> getCategoriesByIds(List<Long> ids) {
        return categoryRepository.findByIdIn(ids);
    }
}
