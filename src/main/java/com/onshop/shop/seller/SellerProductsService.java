package com.onshop.shop.seller;

import java.util.List;

public interface SellerProductsService {
	
	
	List<SellerProductsDTO> getAllProducts(int page, int size);
	
}
