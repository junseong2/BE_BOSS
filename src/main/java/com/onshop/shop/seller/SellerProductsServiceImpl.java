package com.onshop.shop.seller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.onshop.shop.category.Category;
import com.onshop.shop.category.CategoryRepository;
import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.inventory.Inventory;
import com.onshop.shop.inventory.InventoryRepository;
import com.onshop.shop.products.Product;
import com.onshop.shop.products.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SellerProductsServiceImpl implements SellerProductsService {
	
	private final ProductRepository productRepository;
	private final InventoryRepository inventoryRepository;
	private final CategoryRepository categoryRepository;
	
	// 점주 상품 조회
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

	// 점주 상품 추가
	@Override
	public void registerProducts(List<SellerProductsRequestDTO> productsDTO) {
		// (임시) 점주 id
		Long sellerId = 1L;

		// 카테고리 이름들을 한 번에 조회하여 매핑 처리
		List<String> categoryNames = productsDTO.stream()
		                                      .map(SellerProductsRequestDTO::getCategory)
		                                      .distinct()
		                                      .collect(Collectors.toList());
		Map<String, Category> categoryMap = categoryRepository.findByNameIn(categoryNames)
		                                                      .stream()
		                                                      .collect(Collectors.toMap(Category::getName, category -> category));

		// 상품 추가
		List<Product> unsavedProducts = productsDTO.stream().map((product) -> {
		    String categoryName = product.getCategory();
		    Category category = categoryMap.get(categoryName); // 이미 조회된 카테고리 사용

		    if (category == null) {
		        // 카테고리가 없는 경우 처리 (예: 예외 던지기, 기본값 사용 등)
		        throw new IllegalArgumentException("Category not found: " + categoryName);
		    }

		    return Product.builder()
		            .category(category)
		            .name(product.getProductName())
		            .price(product.getPrice())
		            .sellerId(sellerId)
		            .description(product.getDescription())
		            .build();
		}).collect(Collectors.toList());

		List<Product> savedProducts = productRepository.saveAll(unsavedProducts);

		// 재고 추가
		List<Inventory> unsavedInventories = savedProducts.stream().map((product) -> {
		    return Inventory.builder()
		            .product(product)
		            .stock(0)
		            .build();
		}).collect(Collectors.toList());

		inventoryRepository.saveAll(unsavedInventories);

	}

}
