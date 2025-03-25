package com.onshop.shop.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;


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
	List<SellerProductsDTO> getAllProducts(int page, int size); // 모든 상품 조회
	void registerProducts(List<SellerProductsRequestDTO> products); // 상품 추가(다중)
    void removeProducts(SellerProductIdsDTO productsIds); 	// 상품 삭제
    void updateProducts(Long productId, SellerProductsRequestDTO product); // 상품 정보 수정
    List<SellerProductsDTO> searchProducts(String search, int page, int size);
    Product registerProduct(SellerProductsRequestDTO product); // 상품 추가(단일)
    void reigsterProductImages(List<MultipartFile> imaegs, Product product);
    

}