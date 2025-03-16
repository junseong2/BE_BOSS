package com.onshop.shop.category;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/category")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

 // ✅ 특정 카테고리 정보 조회 (카테고리 이름 포함)
    @GetMapping("/{categoryId}")
    public ResponseEntity<CategoryDTO> getCategoryById(@PathVariable Long categoryId) {
        CategoryDTO category = categoryService.getCategoryById(categoryId);
        return ResponseEntity.ok(category);
    }

    // 루트 카테고리 목록
    @GetMapping("/root")
    public List<CategoryDTO> getRootCategories() {
        return categoryService.getRootCategories();
    }

    // 특정 카테고리의 하위 카테고리 목록
    @GetMapping("/{categoryId}/subcategories")
    public List<CategoryDTO> getSubCategories(@PathVariable Long categoryId) {
        return categoryService.getSubCategories(categoryId);
    }
}
