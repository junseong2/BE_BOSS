package com.onshop.shop.seller;

import java.util.List;

public interface SellerProductsService {
	
	
	List<SellerProductsDTO> getAllProducts(int page, int size); // 모든 상품 조회
	void registerProducts(List<SellerProductsRequestDTO> products); // 상품 추가(등록)
    void removeProducts(SellerProductIdsDTO productsIds); 	// 상품 삭제(다중)
    void removeProduct(Long productId); // 상품 삭제(단일)	
}
