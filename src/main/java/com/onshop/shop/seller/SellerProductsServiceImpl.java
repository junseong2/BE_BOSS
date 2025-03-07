package com.onshop.shop.seller;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.products.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerProductsServiceImpl implements SellerProductsService {
	
	private final ProductRepository productRepository;

	@Override
	public List<SellerProductsDTO> getAllProducts(int page, int size) {
		
		Long sellerId = 1L; // 임시
		Pageable pageable = PageRequest.of(page, size);
		
		List<SellerProductsDTO> products =  productRepository.findBySellerId(sellerId, pageable).toList();
		
		if(products == null) {
			throw new ResourceNotFoundException("조회할 상품 목록을 찾을 수 없습니다.") ;
		}
		
		return products;
	}

}
