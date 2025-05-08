package com.onshop.shop.domain.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onshop.shop.domain.product.entity.ProductImage;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

	void deleteByProductProductIdIn(List<Long> productIds);

}
