package com.onshop.shop.domain.category.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.onshop.shop.domain.category.dto.CategoryDTO;
import com.onshop.shop.domain.category.entity.Category;
import com.onshop.shop.domain.category.repository.CategoryRepository;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public List<CategoryDTO> getRootCategories() {
        return categoryRepository.findByParentCategoryIsNull().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public List<CategoryDTO> getSubCategories(Long parentId) {
        return categoryRepository.findByParentCategoryId(parentId).stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public CategoryDTO getCategoryById(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("카테고리를 찾을 수 없습니다."));
        return CategoryDTO.fromEntity(category);
    }

    // 대중분류 전체조회
	@Override
	public List<Category> getCategoryHierarchy() {
		List<Category> categories = categoryRepository.findAllWithChildren();
		return categories;
	}
}
