package com.onshop.shop.seller.products;

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

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerProductsServiceImpl implements SellerProductsService {
    
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;
    
    // 점주 상품 조회
    @Override
    public List<SellerProductsDTO> getAllProducts(int page, int size) {
        Long sellerId = 1L; // 임시
        Pageable pageable = PageRequest.of(page, size);
        
        List<SellerProductsDTO> products = productRepository.findBySellerId(sellerId, pageable).toList();
        
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("조회할 상품 목록을 찾을 수 없습니다.");
        }
        
        return products;
    }

    // 점주 상품 추가
    @Transactional
    @Override
    public void registerProducts(List<SellerProductsRequestDTO> productsDTO) {
        Long sellerId = 1L; // 임시

        List<String> categoryNames = productsDTO.stream()
                                                .map(SellerProductsRequestDTO::getCategoryName)
                                                .distinct()
                                                .collect(Collectors.toList());
        Map<String, Category> categoryMap = categoryRepository.findByNameIn(categoryNames)
                                                              .stream()
                                                              .collect(Collectors.toMap(Category::getName, category -> category));
        
        List<Product> unsavedProducts = productsDTO.stream().map(product -> {
            Category category = categoryMap.get(product.getCategoryName());
            if (category == null) {
                throw new IllegalArgumentException("Category not found: " + product.getCategoryName());
            }
            return Product.builder()
                    .category(category)
                    .name(product.getName())
                    .description(product.getDescription())
                    .price(product.getPrice())
                    .sellerId(sellerId)
                    .build();
        }).collect(Collectors.toList());

        List<Product> savedProducts = productRepository.saveAll(unsavedProducts);

        List<Inventory> unsavedInventories = savedProducts.stream().map(product ->
            Inventory.builder()
                    .product(product)
                    .stock(0L)
                    .build()
        ).collect(Collectors.toList());

        inventoryRepository.saveAll(unsavedInventories);
    }
    
    // 상품 수정
    @Override
    @Transactional
    public void updateProducts(Long productId, SellerProductsRequestDTO productDTO) {
        Long sellerId = 1L;
        
        Product oldProduct = productRepository.findBySellerIdAndProductId(sellerId, productId);
        if (oldProduct == null) {
            throw new ResourceNotFoundException("상품ID:" + productId + " 로 등록된 상품을 찾을 수 없습니다.");
        }
        
        Category category = categoryRepository.findByCategoryName(productDTO.getCategoryName());
        		
  		if(category == null) {
   			throw new ResourceNotFoundException(productDTO.getCategoryName() + "로 등록된 카테고리를 찾을 수 없습니다.");	
   		}
                
        
        oldProduct.setName(productDTO.getName());
        oldProduct.setCategory(category);
        oldProduct.setDescription(productDTO.getDescription());
        oldProduct.setPrice(productDTO.getPrice());
        
        productRepository.save(oldProduct);
    }
    
    // 상품 삭제
    @Override
    @Transactional
    public void removeProducts(SellerProductIdsDTO productsIdsDTO) {
        List<Long> productIds = productsIdsDTO.getIds();
        
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("삭제할 상품 ID 목록이 비어 있습니다.");
        }
        
        productRepository.deleteAllByIdInBatch(productIds);
    }
    
    // 상품 검색
    @Override
    public List<SellerProductsDTO> searchProducts(String search, int page, int size) {
        Long sellerId = 1L; // 임시
        Pageable pageable = PageRequest.of(page, size);
        
        List<SellerProductsDTO> products = productRepository.findByNameAndSellerId(search, sellerId, pageable).toList();
        log.info("products: {}", products);
        
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("조회할 상품 목록을 찾을 수 없습니다.");
        }
        
        return products;
    }
}
