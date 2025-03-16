package com.onshop.shop.seller.products;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.category.Category;
import com.onshop.shop.category.CategoryRepository;
import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.inventory.Inventory;
import com.onshop.shop.inventory.InventoryRepository;
import com.onshop.shop.products.Product;
import com.onshop.shop.products.ProductImage;
import com.onshop.shop.products.ProductImageRepository;
import com.onshop.shop.products.ProductRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SellerProductsServiceImpl implements SellerProductsService {
    
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;
    
    @Value("${file.upload-dir}")  // application.properties에서 경로 정보를 읽어옴
    private String uploadDir;
    
    // 점주 상품 조회
    @Override
    public List<SellerProductsDTO> getAllProducts(int page, int size) {
        Long sellerId = 1L; // 임시
        Pageable pageable = PageRequest.of(page, size);
        
        List<SellerProductsDTO> products = productRepository.findBySellersId(sellerId, pageable);
        
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("조회할 상품 목록을 찾을 수 없습니다.");
        }
        
        return products;
    }

    // 점주 상품 추가(다중)
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
    

    // 상품 저장(단일)    
	@Override
	public Product registerProduct(SellerProductsRequestDTO product) {
		
		Long sellerId = 1L;
		
		String categoryName = product.getCategoryName();
		Category category = categoryRepository.findByCategoryName(categoryName);
		
		// 카테고리 없으면 예외 처리
        if (category == null) {
            throw new IllegalArgumentException("Category not found: " + product.getCategoryName());
        }
        
        //
        Product unsavedProduct =  Product.builder()
                .category(category)
                .name(product.getName())
                .expiryDate(null)
                .description(product.getDescription())
                .price(product.getPrice())
                .sellerId(sellerId)
                .build();
        
        
		return productRepository.save(unsavedProduct);
		
		
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

    // 이미지 업로드
	@Override
	public void reigsterProductImages(List<MultipartFile> images, Product product) {

	    // 이미지 파일 처리
        List<ProductImage> productImages = new ArrayList<>();

        for (MultipartFile image : images) {
            String name = UUID.randomUUID() + "_" + image.getOriginalFilename(); // 이름 중복 방지를 위한 랜덤 이름 설정
            String imageUrl = uploadDir + name;

            // 파일을 서버에 저장 로직
            File fileDir = new File(imageUrl); // 이건 저장경로와 파일이름이 합쳐진 URL임
            
            try {
            	Files.createDirectories(Paths.get(uploadDir)); // 접근 경로가 없으면 해당 경로에 디렉토리 자동 생성
	            image.transferTo(fileDir); // 해당 경로에 실제 이미지를 저장
			
            } catch (IOException e) {
				log.error(e.getMessage());
			
            }

            // ProductImage 객체 
            ProductImage productImage = new ProductImage();
            productImage.setName(name);
            productImage.setImageUrl(imageUrl);
            productImage.setProduct(product);  // 상품과 연결

            productImages.add(productImage);
        }
        
        
        productImageRepository.saveAll(productImages);
		
	}

}
