package com.onshop.shop.store;

import java.util.List;

import com.onshop.shop.product.Product;

import lombok.ToString;

@ToString
public class XXXDTO {

	int  currentPage;
	int  totalItems;
	int  totalPages;
	String  sortOrder;
	List<Product> products;
	
	
	
}
