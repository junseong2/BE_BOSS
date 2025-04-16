package com.onshop.shop.product;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.category.CategoryDTO;
import com.opencsv.exceptions.CsvValidationException;


public interface ProductsService {
    List<ProductsDTO> getAllProducts();
    ProductsDTO getProductById(Long productId);
    List<ProductsDTO> getProductsByCategory(Long categoryId);
    List<ProductsDTO> searchProducts(String query);
    List<Product> getProductsBySellerId(Long sellerId);


    // ✅ 페이징 적용하여 상품 조회
    Page<Product> getAllProductsPage(Long sellerId, int page, int size);
    
    // 상품 ID와 이름을 조회하는 메서드는 추상 메서드로만 선언
    void getProductDetails(Long productId);
    
    Page<Product> getProductsBySeller(Long sellerId, Pageable pageable);
    
    
    /* 판매자 */
	SellerProductsResponseDTO getAllDashboardProducts(int page, int size, String search, Long userId); // 모든 상품 조회
	void registerProducts(List<SellerProductsRequestDTO> products, Long userId); // 상품 추가(다중)
    void removeProducts(SellerProductIdsDTO productsIds, Long userId); 	// 상품 삭제
    Product updateProducts(Long productId, SellerProductsRequestDTO product, Long userId); // 상품 정보 수정
    Product registerProduct(SellerProductsRequestDTO product, Long userId); // 상품 추가(단일)
	void registerProductImages(List<MultipartFile> images, Product product);


    SellerProductsResponseDTO getAllProducts(int page, int size, String search, String sort);
    SellerProductsResponseDTO getAllProducts(Long sellerId, int page, int size, String search, String sort);
    SellerProductsResponseDTO getAllProducts(Long sellerId, int page, int size, String search, String sort, Long categoryId);


    
    public List<CategoryDTO> getUsedCategoriesBySeller(Long sellerId);

    
    // 일간 인기 상품 조회
    public List<Product> getPopularProductsDaily();

    // 주간 인기 상품 조회
    public List<Product> getPopularProductsWeekly();
    // 월간 인기 상품 조회
    public List<Product> getPopularProductsMonthly();

    // 전체 인기 상품 조회
    public List<Product> getAllPopularProducts();
    
    List<Product> getPopularProductsBySellerDaily(Long sellerId);
    List<Product> getPopularProductsBySellerWeekly(Long sellerId);
    List<Product> getPopularProductsBySellerMonthly(Long sellerId);
    List<SellerProductsListDTO> getAllSellerProducts(Long sellerId, int page, int size, String search, String sort, Long categoryId);
    

    ProductDetailResponseDTO getProductDetail(Long productId);


    
    /** 판매자 상품 CSV 업로드*/
    public void uploadProductsCsv(MultipartFile file, Long userId)  throws IOException, CsvValidationException;
}