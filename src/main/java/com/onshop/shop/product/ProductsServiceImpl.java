package com.onshop.shop.product;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.onshop.shop.category.Category;
import com.onshop.shop.category.CategoryRepository;
import com.onshop.shop.exception.ResourceNotFoundException;
import com.onshop.shop.exception.SuccessMessageResponse;
import com.onshop.shop.inventory.Inventory;
import com.onshop.shop.inventory.InventoryRepository;
import com.onshop.shop.product.Product;
import com.onshop.shop.store.Seller;
import com.onshop.shop.store.SellerRepository;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class ProductsServiceImpl implements ProductsService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository; // ✅ 추가
    private final InventoryRepository inventoryRepository;
    private final SellerRepository sellerRepository;

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
    public List<SellerProductsDTO> getAllProducts(int page, int size) {
        Long sellerId = 1L; // 임시
        Pageable pageable = PageRequest.of(page, size);
        
        Page<Product> productPage = productRepository.findBySellerSellerId(sellerId, pageable);
        Page<SellerProductsDTO> dtoPage = productPage.map(product -> {
      
        	
            Long stock = inventoryRepository.
            		findStockByProductId(product.getProductId()).orElse(0L);


            return new SellerProductsDTO(
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getCategory().getName(),
                product.getDescription(),
                stock // ✅ Inventory에서 가져온 stock 값 사용
            );
        });

        List<SellerProductsDTO> products = dtoPage.getContent(); // ✅ Page -> List 변환

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
