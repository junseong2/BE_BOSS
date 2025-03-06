package com.onshop.shop.products;

import org.apache.ibatis.annotations.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByCategoryId(Long categoryId); // 기존 코드
    List<Product> findByCategoryIdIn(List<Long> categoryIds);
    
    @Query("SELECT p FROM Product p JOIN p.category c WHERE p.name LIKE %:query% OR c.name LIKE %:query%")
    public List<Product> searchByNameOrCategory(@Param("query") String query);
}
