package com.onshop.shop.store;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onshop.shop.category.Category;

public interface StoreCategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByIdIn(List<Long> ids);
}
