package com.onshop.shop.domain.product.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.domain.category.entity.Category;
import com.onshop.shop.domain.category.repository.CategoryRepository;
import com.onshop.shop.domain.inventory.entity.Inventory;
import com.onshop.shop.domain.inventory.repository.InventoryRepository;
import com.onshop.shop.domain.product.dto.SellerProductIdsDTO;
import com.onshop.shop.domain.product.dto.SellerProductsDTO;
import com.onshop.shop.domain.product.dto.SellerProductsListDTO;
import com.onshop.shop.domain.product.dto.SellerProductsRequestDTO;
import com.onshop.shop.domain.product.dto.SellerProductsResponseDTO;
import com.onshop.shop.domain.product.entity.Product;
import com.onshop.shop.domain.product.enums.DiscountRate;
import com.onshop.shop.domain.product.repository.ProductRepository;
import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.seller.repository.SellerRepository;
import com.onshop.shop.global.exception.NotAuthException;
import com.onshop.shop.global.exception.ResourceNotFoundException;
import com.onshop.shop.global.file.FileUploadService;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductsSellerServiceImpl implements ProductsSellerService {
	

    private final SellerRepository sellerRepository;
    private final ProductRepository productRepository;
    private final InventoryRepository inventoryRepository;
    private final CategoryRepository categoryRepository;
    private final FileUploadService fileUploadService;
	
	
    //대시보드 상품 조회
    @Override
    public SellerProductsResponseDTO getAllDashboardProducts(int page, int size, String search, Long userId) {
    	
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
    

    // 상품 추가
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
    
    @Override
    public SellerProductsResponseDTO getAllProducts(int page, int size, String search, String sort) {
        Long sellerId = 999L; // TODO: 추후 로그인 정보에서 받아오도록 수정

        Pageable pageable;

        switch ((sort != null ? sort.toLowerCase() : "recommend")) {
        case "low":
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "price"));
            break;
        case "high":
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "price"));
            break;
        case "latest":
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdRegister"));
            break;
        case "popular":
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "overallSales"));
            break;
        case "recommend":
        default:
            pageable = PageRequest.of(page, size);
            break;
    }

  
        List<SellerProductsDTO> products = productRepository.findBySellerSellerIdAndSearch(sellerId, search, pageable).toList();
        Long productCount = productRepository.countBySellerSellerIdAndName(sellerId, search);

        if (products.isEmpty()) {
            throw new ResourceNotFoundException("조회할 상품 목록을 찾을 수 없습니다.");
        }

        return SellerProductsResponseDTO.builder()
                .products(products)
                .totalCount(productCount)
                .build();
    }

    @Override
    public SellerProductsResponseDTO getAllProducts(Long sellerId, int page, int size, String search, String sort) {
        Pageable pageable = PageRequest.of(page, size);

        log.info("🔍 정렬 방식: {}", sort);

        Page<SellerProductsDTO> productsPage;

        switch (sort.toLowerCase()) {
        case "low":
            productsPage = productRepository.findBySellerSellerIdAndSearchOrderByPriceAsc(sellerId, search, pageable);
            break;
        case "high":
            productsPage = productRepository.findBySellerSellerIdAndSearchOrderByPriceDesc(sellerId, search, pageable);
            break;
        case "latest":
            productsPage = productRepository.findBySellerSellerIdAndSearchOrderByCreatedRegisterDesc(sellerId, search, pageable);
            break;
        case "popular":
            productsPage = productRepository.findBySellerSellerIdAndSearchOrderByOverallSalesDesc(sellerId, search, pageable);
            break;
        default: // recommend 또는 unknown -> 기본정렬
            productsPage = productRepository.findBySellerSellerIdAndSearch(sellerId, search, pageable);
    }

        if (productsPage.isEmpty()) {
            throw new ResourceNotFoundException("조회할 상품 목록을 찾을 수 없습니다.");
        }

        return SellerProductsResponseDTO.builder()
                .products(productsPage.getContent())
                .totalCount(productsPage.getTotalElements())
                .build();
    }
    @Override
    public List<SellerProductsListDTO> getAllSellerProducts(Long sellerId, int page, int size, String search, String sort, Long categoryId) {
        Pageable pageable = PageRequest.of(page, size);
        Page<SellerProductsListDTO> productsPage;

        // 카테고리 ID가 있을 경우 쿼리 분기 처리
        if (categoryId != null) {
            switch (sort.toLowerCase()) {
                case "low":
                    productsPage = productRepository.findSellerProductsByCategoryAndPriceAsc(sellerId, categoryId, search, pageable);
                    break;
                case "high":
                    productsPage = productRepository.findSellerProductsByCategoryAndPriceDesc(sellerId, categoryId, search, pageable);
                    break;
                case "latest":
                    productsPage = productRepository.findSellerProductsByCategoryAndCreatedDateDesc(sellerId, categoryId, search, pageable);
                    break;
                case "popular":
                    productsPage = productRepository.findSellerProductsByCategoryAndSalesDesc(sellerId, categoryId, search, pageable);
                    break;
                default:
                    productsPage = productRepository.findSellerProductsByCategory(sellerId, categoryId, search, pageable);
            }
        } else {
            switch (sort.toLowerCase()) {
                case "low":
                    productsPage = productRepository.findSellerProductsByPriceAsc(sellerId, search, pageable);
                    break;
                case "high":
                    productsPage = productRepository.findSellerProductsByPriceDesc(sellerId, search, pageable);
                    break;
                case "latest":
                    productsPage = productRepository.findSellerProductsByCreatedDateDesc(sellerId, search, pageable);
                    break;
                case "popular":
                    productsPage = productRepository.findSellerProductsBySalesDesc(sellerId, search, pageable);
                    break;
                default:
                    productsPage = productRepository.findSellerProductsBySearch(sellerId, search, pageable);
            }
        }

        // 상품이 없을 경우 예외 처리
        if (productsPage.isEmpty()) {
            throw new ResourceNotFoundException("조회할 상품 목록을 찾을 수 없습니다.");
        }

        return productsPage.getContent(); // SellerProductsListDTO 반환
    }


    
    @Override
    public SellerProductsResponseDTO getAllProducts(Long sellerId, int page, int size, String search, String sort, Long categoryId) {
        Pageable pageable = PageRequest.of(page, size);
        log.info("🔍 정렬 방식: {}, 카테고리 ID: {}", sort, categoryId);

        Page<SellerProductsDTO> productsPage;

        // 정렬 + categoryId 여부에 따라 쿼리 분기
        if (categoryId != null) {
            switch (sort.toLowerCase()) {
                case "low":
                    productsPage = productRepository.findBySellerAndCategoryOrderByPriceAsc(sellerId, categoryId, search, pageable);
                    break;
                case "high":
                    productsPage = productRepository.findBySellerAndCategoryOrderByPriceDesc(sellerId, categoryId, search, pageable);
                    break;
                case "latest":
                    productsPage = productRepository.findBySellerAndCategoryOrderByCreatedRegisterDesc(sellerId, categoryId, search, pageable);
                    break;
                case "popular":
                    productsPage = productRepository.findBySellerAndCategoryOrderByOverallSalesDesc(sellerId, categoryId, search, pageable);
                    break;
                default:
                    productsPage = productRepository.findBySellerAndCategory(sellerId, categoryId, search, pageable);
            }
        } else {
            switch (sort.toLowerCase()) {
                case "low":
                    productsPage = productRepository.findBySellerSellerIdAndSearchOrderByPriceAsc(sellerId, search, pageable);
                    break;
                case "high":
                    productsPage = productRepository.findBySellerSellerIdAndSearchOrderByPriceDesc(sellerId, search, pageable);
                    break;
                case "latest":
                    productsPage = productRepository.findBySellerSellerIdAndSearchOrderByCreatedRegisterDesc(sellerId, search, pageable);
                    break;
                case "popular":
                    productsPage = productRepository.findBySellerSellerIdAndSearchOrderByOverallSalesDesc(sellerId, search, pageable);
                    break;
                default:
                    productsPage = productRepository.findBySellerSellerIdAndSearch(sellerId, search, pageable);
            }
        }

        if (productsPage.isEmpty()) {
            throw new ResourceNotFoundException("조회할 상품 목록을 찾을 수 없습니다.");
        }

        return SellerProductsResponseDTO.builder()
                .products(productsPage.getContent())
                .totalCount(productsPage.getTotalElements())
                .build();
    }
    
    // 상품 저장(단일) 
	@Override
	public Product registerProduct(SellerProductsRequestDTO product, Long userId) {
		
	    Seller seller = sellerRepository.findByUserId(userId)
	                .orElseThrow(() -> new NotAuthException("판매자만 이용 가능합니다."));

		
		String categoryName = product.getCategoryName();
		Category category = categoryRepository.findByCategoryName(categoryName);
		
		// 카테고리 없으면 예외 처리
        if (category == null) {
            throw new ResourceNotFoundException("Category not found: " + product.getCategoryName());
        }
        
 
        
        Product unsavedProduct =  Product.builder()
                .category(category)
                .name(product.getName())
                .expiryDate(LocalDateTime.parse(product.getExpiryDate()))
                .description(product.getDescription())
                .price(product.getPrice())
                .originPrice(product.getOriginPrice())
                .discountRate(DiscountRate.fromRate(product.getDiscountRate()))
                .isDiscount(product.getDiscountRate() >0 ? true : false) // 할인율이 0보다 크면 할인 적용
                .seller(seller)
                .build();
        
        unsavedProduct.applyDiscount(); // 할인율을 적용한 price 를 컬럼에 반영
    
        // 상품 정보 저장
        log.info("저장되기 전의 수정된 product:{}", unsavedProduct);
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
    public Product updateProducts(Long productId, SellerProductsRequestDTO productDTO, Long userId) {
	   Seller seller = sellerRepository.findByUserId(userId)
                .orElseThrow(() -> new NotAuthException("판매자만 이용 가능합니다."));

	   Long sellerId = seller.getSellerId();
	    
       Product oldProduct = productRepository.findBySeller_SellerIdAndProductId(sellerId, productId);
        if (oldProduct == null) {
            throw new ResourceNotFoundException("상품ID:" + productId + " 로 등록된 상품을 찾을 수 없습니다.");
        }
        
       Category category = categoryRepository.findByCategoryName(productDTO.getCategoryName());
        		
  		if(category == null) {
   			throw new ResourceNotFoundException(productDTO.getCategoryName() + "로 등록된 카테고리를 찾을 수 없습니다.");	
   		}
                
        
        oldProduct.setName(productDTO.getName());
        oldProduct.setCategory(category);
        oldProduct.setExpiryDate(LocalDateTime.parse(productDTO.getExpiryDate()));
        oldProduct.setDiscountRate(DiscountRate.fromRate(productDTO.getDiscountRate()));
        oldProduct.setIsDiscount(productDTO.getDiscountRate() > 0 ? true : false);
        oldProduct.setOriginPrice(productDTO.getOriginPrice());
        oldProduct.setDescription(productDTO.getDescription());
        oldProduct.setPrice(productDTO.getPrice());
        
        oldProduct.applyDiscount(); // 할인 가격 적용
        
        Product product = productRepository.save(oldProduct);
        
        // 재고 저장
        Inventory inventory = inventoryRepository.findByProduct(oldProduct);
        
        
        // 인벤토리가 없으면 해당 상품에 대한 인벤토리를 만들어줌
        if(inventory == null) {
        inventory = Inventory.builder()
        	.minStock(productDTO.getMinStock())
        	.stock(productDTO.getStock())
        	.product(product)
        	.seller(seller)
        	.build();
        
        // 존재하면 
        } else {
        	inventory.setStock(productDTO.getStock());
        	inventory.setMinStock(productDTO.getMinStock());
        }
        
        inventoryRepository.save(inventory);
        
        return product;
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
        List<String> imageNames = images.stream().map(fileUploadService::upload).toList();
        

        // g_image 업데이트 (파일명 리스트를 ,로 구분된 문자열로 변환하여 저장)
        String gImageString = String.join(",", imageNames);
        product.setGImage(gImageString);
        productRepository.save(product);
    }
    
    
    @Override
    public Page<Product> getAllProductsPage(Long sellerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return productRepository.findBySellerSellerId(sellerId, pageable);
    
    }


    @Override
    public Page<Product> getProductsBySeller(Long sellerId, Pageable pageable) {
        return productRepository.findBySellerSellerId(sellerId, pageable);
    }
	


    /** 판매자 상품 CSV 업로드
     * @throws IOException 
     * @throws CsvValidationException */
	@Override
	public void uploadProductsCsv(MultipartFile file, Long userId) throws IOException, CsvValidationException {
		
        try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] nextLine;
            List<Product> products = new ArrayList<>();
 
            csvReader.readNext(); // 헤더는 건너감

            while ((nextLine = csvReader.readNext()) != null) {
            	
            }
        }		
	}

}