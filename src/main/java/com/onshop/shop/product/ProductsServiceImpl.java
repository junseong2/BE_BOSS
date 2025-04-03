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
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.category.Category;
import com.onshop.shop.category.CategoryDTO;
import com.onshop.shop.category.CategoryRepository;
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
    private final ProductImageRepository productImageRepository; // 상품 이미지 리포
    
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
        return ProductsDTO.fromEntity(product);
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
    public SellerProductsResponseDTO getAllProducts(int page, int size, String search) {
        Long sellerId = 999L; // 임시
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
    
    /** 판매자 쿼리 */
    // 점주 상품 조회
    @Override
    public SellerProductsResponseDTO getAllProducts(int page, int size, String search, String sort) {
        Long sellerId = 999L; // TODO: 추후 로그인 정보에서 받아오도록 수정

        Pageable pageable;

        switch (sort.toLowerCase()) {
        case "low":
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "price"));
            break;
        case "high":
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "price"));
            break;
        case "latest":
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdRegister")); // ✅ 필드명 주의!
            break;
        case "popular":
            pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "overallSales")); // 또는 weeklySales
            break;
        case "recommend":
        default:
            pageable = PageRequest.of(page, size); // 특별한 정렬 없음
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


    // 점주 상품 추가
    @Transactional
    @Override
    public void registerProducts(List<SellerProductsRequestDTO> productsDTO) {
        Long sellerId = 999L; // 임시
        Seller seller = sellerRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("Seller not found: " + sellerId));

        List<String> categoryNames = productsDTO.stream()
                                                .map(SellerProductsRequestDTO::getCategoryName)
                                                .distinct()
                                                .collect(Collectors.toList());

        Map<String, Category> categoryMap = categoryRepository.findByNameIn(categoryNames)
                                                              .stream()
                                                              .collect(Collectors.toMap(Category::getName, category -> category));
        
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

        

        List<Product> savedProducts = productRepository.saveAll(unsavedProducts);

        List<Inventory> unsavedInventories = savedProducts.stream().map(product ->
            Inventory.builder()
                    .product(product)
                    .stock(0L)
                    .build()
        ).collect(Collectors.toList());

        inventoryRepository.saveAll(unsavedInventories);
    }
    
    // 상품 저장(단일) -> TODO: 병합 시 이 친구를 살려야 합니다.   
	@Override
	public Product registerProduct(SellerProductsRequestDTO product) {
		
		Long sellerId = 999L;
		
		String categoryName = product.getCategoryName();
		Category category = categoryRepository.findByCategoryName(categoryName);
		
		// 카테고리 없으면 예외 처리
        if (category == null) {
            throw new IllegalArgumentException("Category not found: " + product.getCategoryName());
        }
        
        
        Seller seller = sellerRepository.findById(sellerId).orElse(null);
       
        
        Product unsavedProduct =  Product.builder()
                .category(category)
                .name(product.getName())
                .expiryDate(null)
                .description(product.getDescription())
                .price(product.getPrice())
                .seller(seller)
                .build();
        
        
		return productRepository.save(unsavedProduct);
		
		
	}
    
    // 상품 수정
    @Override
    @Transactional
    public void updateProducts(Long productId, SellerProductsRequestDTO productDTO) {
        Long sellerId = 999L;
        
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
    
    
    // 이미지 업로드(다중) --> TODO: 이 친구 병합 시 살립시다.
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
    @Override
    public List<Product> getPopularProductsDaily() {
        return productRepository.findAllByOrderByDailySalesDesc();  // List<Product>로 반환
    }

    @Override
    public List<Product> getPopularProductsWeekly() {
        return productRepository.findAllByOrderByWeeklySalesDesc(); // List<Product>로 반환
    }

    @Override
    public List<Product> getPopularProductsMonthly() {
        return productRepository.findAllByOrderByMonthlySalesDesc(); // List<Product>로 반환
    }

    @Override
    public List<Product> getAllPopularProducts() {
        return productRepository.findAllByOrderByOverallSalesDesc(); // List<Product>로 반환
    }

    @Override
    public List<CategoryDTO> getUsedCategoriesBySeller(Long sellerId) {
        List<Long> usedCategoryIds = productRepository.findDistinctCategoryIdsBySellerId(sellerId);
        List<Category> categories = categoryRepository.findAllById(usedCategoryIds);
        
        return categories.stream()
            .map(category -> new CategoryDTO(category.getId(), category.getName()))
            .collect(Collectors.toList());
    }



    
    @Override
    public List<SellerProductsDTO> getPopularProductsBySeller(Long sellerId, String sortBy) {
        return switch (sortBy.toLowerCase()) {
            case "daily" -> productRepository.findPopularDailyBySellerId(sellerId);
            case "weekly" -> productRepository.findPopularWeeklyBySellerId(sellerId);
            case "monthly" -> productRepository.findPopularMonthlyBySellerId(sellerId);
            default -> productRepository.findPopularOverallBySellerId(sellerId);
        };
    }


}