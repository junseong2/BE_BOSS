package com.onshop.shop.product;

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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.category.Category;
import com.onshop.shop.category.CategoryRepository;
import com.onshop.shop.exception.NotAuthException;
import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.inventory.Inventory;
import com.onshop.shop.inventory.InventoryRepository;
import com.onshop.shop.seller.Seller;
import com.onshop.shop.seller.SellerRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductsServiceImpl implements ProductsService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository; // ✅ 추가
    private final InventoryRepository inventoryRepository;
    private final SellerRepository sellerRepository;
    
    @Value("${file.upload-dir}")  // application.properties에서 경로 정보를 읽어옴
    private String uploadDir;
    

    @Override
    public List<ProductsDTO> getAllProducts() {
        return productRepository.findAll().stream()
                .map(ProductsDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    public ProductsDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품이 존재하지 않습니다."));
        
        Long oldViewCount = product.getViewCount();
        product.setViewCount(oldViewCount+1);
        
        
        
        return ProductsDTO.fromEntity(productRepository.save(product));
    }

    @Override
    public Page<Product> getAllProductsPage(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findBySellerSellerId(sellerId, pageable);
    
    }

    
    
    @Override
    public List<ProductsDTO> getProductsByCategory(Long categoryId) {
        // ✅ 1. 해당 카테고리의 하위 카테고리 ID 가져오기
        List<Category> subcategories = categoryRepository.findByParentCategoryId(categoryId);
        List<Long> categoryIds = subcategories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        // ✅ 2. 선택한 카테고리 ID도 포함하여 검색
        categoryIds.add(categoryId);

        // ✅ 3. 해당 카테고리 + 하위 카테고리에 속한 상품 조회
        List<Product> products = productRepository.findByCategoryIdIn(categoryIds);
        return products.stream()
                .map(ProductsDTO::fromEntity)
                .collect(Collectors.toList());
    }
    
    
    @Override
    public Page<Product> getProductsBySeller(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerSellerId(sellerId, pageable);
    }

    
 // 상품 검색
    @Override
    public List<ProductsDTO> searchProducts(String query) {
        // 상품명 또는 카테고리명으로 검색
        List<Product> products = productRepository.searchByNameOrCategory(query);

        // Product -> ProductsDTO 변환
        return products.stream()
                       .map(ProductsDTO::fromEntity)  // Product -> ProductsDTO로 변환
                       .collect(Collectors.toList());
    }
    

    @Override
    public List<Product> getProductsBySellerId(Long sellerId) {
        return productRepository.findBySellerSellerId(sellerId);
    }
    @Override
    public void getProductDetails(Long productId) {
        // Fetch the product by its ID
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        
        // Print product details
        System.out.println("상품 ID: " + product.getProductId());
        System.out.println("상품 이름: " + product.getName());
    }
    
    
    /** 판매자 쿼리 */
    // 점주 상품 조회
    @Override
    public SellerProductsResponseDTO getAllProducts(int page, int size, String search, Long userId) {
    	
    	Seller seller = sellerRepository.findByUserId(userId).orElseThrow(()->
    		new NotAuthException("판매자만 이용 가능합니다.")
    	);
        Long sellerId = seller.getSellerId();
        Pageable pageable = PageRequest.of(page, size);
        
        List<SellerProductsDTO> products = productRepository.findBySellerSellerIdAndSearch(sellerId, search, pageable).toList();
        Long productCount = productRepository.countBySellerSellerIdAndName(sellerId,search);
        

        if (products.isEmpty()) {
            throw new ResourceNotFoundException("조회할 상품 목록을 찾을 수 없습니다.");
        }
        
        
        return SellerProductsResponseDTO.builder()
        		.products(products)
        		.totalCount(productCount)
        		.build();

    }

    // 점주 상품 추가
    @Transactional
    @Override
    public void registerProducts(List<SellerProductsRequestDTO> productsDTO, Long userId) {
     
        Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotAuthException("판매자만 이용 가능합니다."));

        
        // 카테고리 목록 조회
        List<String> categoryNames = productsDTO.stream()
                                                .map(SellerProductsRequestDTO::getCategoryName)
                                                .distinct()
                                                .collect(Collectors.toList());

        
        // 카테고리 목록에 존재한다면, 카테고리 이름을 키로, 카테고리 객체를 값으로 한 맵 객체를 생성
        Map<String, Category> categoryMap = categoryRepository.findByNameIn(categoryNames)
                                                              .stream()
                                                              .collect(Collectors.toMap(Category::getName, category -> category));
        
        // 생성한 카테고리 맵 객체에 저장된 카테고리 정보와 저장하고자 하는 상품의 카테고리가 매칭 되는 경우 Product 엔티티에 저장
        List<Product> unsavedProducts = productsDTO.stream()
            .map(productDTO -> {
                Category category = categoryMap.get(productDTO.getCategoryName());
                if (category == null) {
                    throw new IllegalArgumentException("Category not found: " + productDTO.getCategoryName());
                }

                return Product.builder()
                        .category(category)
                        .seller(seller)
                        .name(productDTO.getName())
                        .description(productDTO.getDescription())
                        .price(productDTO.getPrice())
                        .build();
            })
            .collect(Collectors.toList());

        
        // 엔티티를 실제 데이터베이스로 저장
        List<Product> savedProducts = productRepository.saveAll(unsavedProducts);

        // 추가된 상품의 초기 재고를 설정
        List<Inventory> unsavedInventories = savedProducts.stream().map(product ->
            Inventory.builder()
                    .product(product)
                    .stock(0L)
                    .minStock(0L)
                    .build()
        ).collect(Collectors.toList());

        inventoryRepository.saveAll(unsavedInventories);
    }
    
    // 상품 저장(단일) -> TODO: 병합 시 이 친구를 살려야 합니다.   
	@Override
	public Product registerProduct(SellerProductsRequestDTO product, Long userId) {
		
	    Seller seller = sellerRepository.findByUserId(userId)
	                .orElseThrow(() -> new NotAuthException("판매자만 이용 가능합니다."));

		
		String categoryName = product.getCategoryName();
		Category category = categoryRepository.findByCategoryName(categoryName);
		
		// 카테고리 없으면 예외 처리
        if (category == null) {
            throw new IllegalArgumentException("Category not found: " + product.getCategoryName());
        }
        
 
        
        Product unsavedProduct =  Product.builder()
                .category(category)
                .name(product.getName())
                .expiryDate(null)
                .description(product.getDescription())
                .price(product.getPrice())
                .seller(seller)
                .build();
        
        
    
        // 상품 정보 저장
        Product savedProduct = productRepository.save(unsavedProduct);
        
        // 초기 재고 저장
        inventoryRepository.save(        
        		Inventory.builder()
                .seller(seller)
                .minStock(product.getMinStock())
                .stock(product.getStock())
                .product(savedProduct)
                .build());

        
		return savedProduct ;
		
		
	}
    
    // 상품 수정
    @Override
    @Transactional
    public void updateProducts(Long productId, SellerProductsRequestDTO productDTO, Long userId) {
	    Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotAuthException("판매자만 이용 가능합니다."));

	    Long sellerId = seller.getSellerId();
	    
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
    public void removeProducts(SellerProductIdsDTO productsIdsDTO, Long userId) {
    	
    	
	    Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotAuthException("판매자만 이용 가능합니다."));
	    
        List<Long> productIds = productsIdsDTO.getIds();
        
        log.info("ids:{}",productIds);
        
        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("삭제할 상품 ID 목록이 비어 있습니다.");
        }
        
        productRepository.deleteAllByIdInBatchAndSeller(productsIdsDTO.getIds(), seller);
    }
    
    
    // 이미지 업로드
    @Override
    public void registerProductImages(List<MultipartFile> images, Product product) {
        // 이미지 파일 처리
        List<String> imageNames = new ArrayList<>(); // g_image 저장용 리스트

        for (MultipartFile image : images) {
            String name = UUID.randomUUID() + "_" + image.getOriginalFilename(); // 랜덤 파일명 생성
            String imageUrl = uploadDir + name;

            // 파일을 서버에 저장하는 로직
            File fileDir = new File(imageUrl);
            try {
                Files.createDirectories(Paths.get(uploadDir)); // 디렉토리 자동 생성
                image.transferTo(fileDir); // 이미지 저장
            } catch (IOException e) {
                log.error(e.getMessage());
            }

            imageNames.add(name); // g_image 저장을 위해 파일명 추가
        }

        // g_image 업데이트 (파일명 리스트를 ,로 구분된 문자열로 변환하여 저장)
        String gImageString = String.join(",", imageNames);
        product.setGImage(gImageString);
        productRepository.save(product);
    }
    
    
    // 이미지 업로드(다중) --> TODO: 이 친구 병합 시 살립시다.
//    @Override
//	public void reigsterProductImages(List<MultipartFile> images, Product product) {
//
//	    // 이미지 파일 처리
//        List<ProductImage> productImages = new ArrayList<>();
//
//        for (MultipartFile image : images) {
//            String name = UUID.randomUUID() + "_" + image.getOriginalFilename(); // 이름 중복 방지를 위한 랜덤 이름 설정
//            String imageUrl = uploadDir + name;
//
//            // 파일을 서버에 저장 로직
//            File fileDir = new File(imageUrl); // 이건 저장경로와 파일이름이 합쳐진 URL임
//            
//            try {
//            	Files.createDirectories(Paths.get(uploadDir)); // 접근 경로가 없으면 해당 경로에 디렉토리 자동 생성
//	            image.transferTo(fileDir); // 해당 경로에 실제 이미지를 저장
//			
//            } catch (IOException e) {
//				log.error(e.getMessage());
//			
//            }
//
//            // ProductImage 객체 
//            ProductImage productImage = new ProductImage();
//            productImage.setName(name);
//            productImage.setImageUrl(imageUrl);
//            productImage.setProduct(product);  // 상품과 연결
//
//            productImages.add(productImage);
//        }
//        
//        
//        productImageRepository.saveAll(productImages);
//		
//	}   
}