package com.onshop.shop.domain.product.service;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.NotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.onshop.shop.domain.category.dto.CategoryDTO;
import com.onshop.shop.domain.category.entity.Category;
import com.onshop.shop.domain.category.repository.CategoryRepository;
import com.onshop.shop.domain.product.dto.ProductDetailResponseDTO;
import com.onshop.shop.domain.product.dto.ProductsDTO;
import com.onshop.shop.domain.product.entity.Product;
import com.onshop.shop.domain.product.repository.ProductRepository;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductsBuyerServiceImpl implements ProductsBuyerService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository; 
    
    @Value("${file.upload-dir}")  // application.properties에서 경로 정보를 읽어옴
    private String uploadDir;
    
    
    /**
     * 모든 상품을 조회하여 `ProductsDTO` 형식으로 반환합니다.
     * 
     * @param page 조회할 페이지 번호
     * @param size 한 페이지에 조회할 상품 수
     * @return 상품 리스트
     */
    @Override
    public List<ProductsDTO> getAllProducts(int page, int size) {
    	Pageable pageable = PageRequest.of(page, size);
        return productRepository.findAll(pageable).stream()
                .map(ProductsDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 상품 ID로 상품을 조회하여 상품 상세 정보를 반환합니다.
     * 조회된 상품의 조회수는 1 증가시킵니다.
     * 
     * @param productId 조회할 상품의 ID
     * @return 조회된 상품의 상세 정보
     */
    @Override
    public ProductsDTO getProductById(Long productId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("요청한 상품 정보가 없습니다."));
        
        Long oldViewCount = product.getViewCount();
        product.setViewCount(oldViewCount + 1);  // 조회수 증가
        
        return ProductsDTO.fromEntity(productRepository.save(product));
    }

    
    /**
     * 카테고리 ID와 하위 카테고리 ID에 속하는 상품들을 조회하여 반환합니다.
     * 
     * @param categoryId 조회할 카테고리의 ID
     * @param page 조회할 페이지 번호
     * @param size 한 페이지에 조회할 상품 수
     * @return 해당 카테고리에 속한 상품들의 리스트
     */
    @Override
    public List<ProductsDTO> getProductsByCategory(Long categoryId, int page, int size) {
    	Pageable pageable = PageRequest.of(page, size);
    	
        // 해당 카테고리의 하위 카테고리 ID 리스트 조회
        List<Category> subcategories = categoryRepository.findByParentCategoryId(categoryId);
        List<Long> categoryIds = subcategories.stream()
                .map(Category::getId)
                .collect(Collectors.toList());

        // 선택한 카테고리 ID도 포함
        categoryIds.add(categoryId);
        
        log.info("카테고리 ids:{}", categoryIds);
        
        // 해당 카테고리와 하위 카테고리에 속한 상품 조회
        List<ProductsDTO> products = productRepository.findByCategoryIdIn(categoryIds, pageable).toList();
        
        log.info("카테고리별 상품조회:{}", products);
        
        return null;  // 현재는 빈 리스트 반환, 주석을 제거하고 리턴값을 설정해야 함
    }

    
    /**
     * 상품명 또는 카테고리명으로 상품을 검색하여 반환합니다.
     * 
     * @param query 검색할 키워드
     * @return 검색된 상품들의 리스트
     */
    @Override
    public List<ProductsDTO> searchProducts(String query) {
        List<Product> products = productRepository.searchByNameOrCategory(query);

        // Product -> ProductsDTO 변환
        return products.stream()
                       .map(ProductsDTO::fromEntity)
                       .collect(Collectors.toList());
    }
    

    /**
     * 판매자 ID로 상품을 조회하여 해당 판매자의 상품 리스트를 반환합니다.
     * 
     * @param sellerId 판매자 ID
     * @return 해당 판매자의 상품 리스트
     */
    @Override
    public List<Product> getProductsBySellerId(Long sellerId) {
        return productRepository.findBySellerSellerId(sellerId);
    }
    
    
    /**
     * 상품 ID로 상품의 상세 정보를 조회합니다.
     * 
     * @param productId 조회할 상품 ID
     */
    @Override
    public void getProductDetails(Long productId) {
        // 상품 조회
        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        
        // 상품 정보 출력
        System.out.println("상품 ID: " + product.getProductId());
        System.out.println("상품 이름: " + product.getName());
    }
    

    /**
     * 일일 판매량 기준으로 인기 상품을 조회합니다.
     * 
     * @param page 조회할 페이지 번호
     * @param size 한 페이지에 조회할 상품 수
     * @return 인기 상품 리스트
     */
    @Override
    public List<Product> getPopularProductsDaily(int page, int size) {
    	Pageable pageable = PageRequest.of(page, size);
    	log.info("page:{}, size:{}", page, size);
    	
        return productRepository.findAllByOrderByDailySalesDesc(pageable);  // 인기 상품 반환 
    }

    /**
     * 주간 판매량 기준으로 인기 상품을 조회합니다.
     * 
     * @return 인기 상품 리스트
     */
    @Override
    public List<Product> getPopularProductsWeekly() {
        return productRepository.findAllByOrderByWeeklySalesDesc(); // 인기 상품 반환
    }

    /**
     * 월간 판매량 기준으로 인기 상품을 조회합니다.
     * 
     * @return 인기 상품 리스트
     */
    @Override
    public List<Product> getPopularProductsMonthly() {
        return productRepository.findAllByOrderByMonthlySalesDesc(); // 인기 상품 반환
    }

    /**
     * 전체 판매량 기준으로 인기 상품을 조회합니다.
     * 
     * @return 인기 상품 리스트
     */
    @Override
    public List<Product> getAllPopularProducts() {
        return productRepository.findAllByOrderByOverallSalesDesc(); // 인기 상품 반환
    }

    /**
     * 판매자 ID로 판매자가 사용한 카테고리 목록을 조회하여 반환합니다.
     * 
     * @param sellerId 판매자 ID
     * @return 판매자가 사용한 카테고리 리스트
     */
    @Override
    public List<CategoryDTO> getUsedCategoriesBySeller(Long sellerId) {
        List<Long> usedCategoryIds = productRepository.findDistinctCategoryIdsBySellerId(sellerId);
        List<Category> categories = categoryRepository.findAllById(usedCategoryIds);
        
        return categories.stream()
            .map(category -> new CategoryDTO(category.getId(), category.getName()))
            .collect(Collectors.toList());
    }

    /**
     * 판매자 ID로 일일 판매량 기준의 인기 상품을 조회합니다.
     * 
     * @param sellerId 판매자 ID
     * @return 인기 상품 리스트
     */
    @Override
    public List<Product> getPopularProductsBySellerDaily(Long sellerId) {
        return productRepository.findBySeller_SellerIdOrderByDailySalesDesc(sellerId);
    }

    /**
     * 판매자 ID로 주간 판매량 기준의 인기 상품을 조회합니다.
     * 
     * @param sellerId 판매자 ID
     * @return 인기 상품 리스트
     */
    @Override
    public List<Product> getPopularProductsBySellerWeekly(Long sellerId) {
    	return productRepository.findBySeller_SellerIdOrderByWeeklySalesDesc(sellerId);
    }

    /**
     * 판매자 ID로 월간 판매량 기준의 인기 상품을 조회합니다.
     * 
     * @param sellerId 판매자 ID
     * @return 인기 상품 리스트
     */
    @Override
    public List<Product> getPopularProductsBySellerMonthly(Long sellerId) {
    	return productRepository.findBySeller_SellerIdOrderByMonthlySalesDesc(sellerId);
    }
    
    /**
     * 상품 ID로 상품 상세 정보와 판매자 정보를 포함한 상품 상세 정보를 조회합니다.
     * 
     * @param productId 상품 ID
     * @return 상품 상세 정보
     */
    @Override
    public ProductDetailResponseDTO getProductDetail(Long productId) {
        Product product = productRepository.findDetailWithSellerById(productId)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
        return ProductDetailResponseDTO.fromEntity(product);
    }
}
