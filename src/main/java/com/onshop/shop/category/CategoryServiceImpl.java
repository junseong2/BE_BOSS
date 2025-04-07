package com.onshop.shop.category;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new RuntimeException("카테고리를 찾을 수 없습니다."));
        return CategoryDTO.fromEntity(category);
    }

    // 대중분류 전체조회
	@Override
	public List<Category> getCategoryHierarchy() {
		List<Category> categories = categoryRepository.findAllWithChildren();
		return categories;
	}
}
