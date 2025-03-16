package com.onshop.shop.category;

import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // parentCategory가 없는 (루트) 카테고리 조회
    List<Category> findByParentCategoryIsNull();

    // 특정 parentCategoryId를 가진 서브 카테고리 조회
    List<Category> findByParentCategoryId(Long parentId);


    // 카테고리 이름으로 카테고리 조회
    @Query("SELECT c FROM Category c WHERE c.name = :categoryName")
    Category findByCategoryName(@Param("categoryName") String categoryName);

    // 포함되는 카테고리를 한 번에 조회
    List<Category> findByNameIn(List<String> categoryNames);
}


