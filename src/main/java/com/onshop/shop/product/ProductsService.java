package com.onshop.shop.product;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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

}
