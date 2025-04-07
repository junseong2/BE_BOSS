package com.onshop.shop.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

	void deleteByProductProductIdIn(List<Long> productIds);

}
