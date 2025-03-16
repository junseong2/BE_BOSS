package com.onshop.shop.products;

import java.util.List;

public interface ProductsService {
    List<ProductsDTO> getAllProducts();
    ProductsDTO getProductById(Long productId);
    List<ProductsDTO> getProductsByCategory(Long categoryId);
    List<ProductsDTO> searchProducts(String query);
    List<Product> getProductsBySellerId(Long sellerId);



    // 상품 ID와 이름을 조회하는 메서드는 추상 메서드로만 선언
    void getProductDetails(Long productId);


}
