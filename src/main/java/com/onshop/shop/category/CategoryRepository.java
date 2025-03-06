package com.onshop.shop.category;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // parentCategory가 없는 (루트) 카테고리 조회
    List<Category> findByParentCategoryIsNull();

    // 특정 parentCategoryId를 가진 서브 카테고리 조회
    List<Category> findByParentCategoryId(Long parentId);
}


