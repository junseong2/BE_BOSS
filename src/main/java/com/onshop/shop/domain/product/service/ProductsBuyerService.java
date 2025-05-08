package com.onshop.shop.domain.product.service;

import java.util.List;

import com.onshop.shop.domain.category.dto.CategoryDTO;
import com.onshop.shop.domain.product.dto.ProductDetailResponseDTO;
import com.onshop.shop.domain.product.dto.ProductsDTO;
import com.onshop.shop.domain.product.entity.Product;

/**
 * 사용자 관점의 상품 서비스 인터페이스
 */
public interface ProductsBuyerService {

    /**
     * 모든 상품을 페이지네이션 없이 조회합니다.
     */
    List<ProductsDTO> getAllProducts(int page, int size);

    /**
     * 상품 ID로 상세 정보를 조회합니다.
     */
    ProductsDTO getProductById(Long productId);

    /**
     * 카테고리별 상품 목록을 조회합니다.
     */
    List<ProductsDTO> getProductsByCategory(Long categoryId, int page, int size);

    /**
     * 상품 검색 쿼리로 결과를 조회합니다.
     */
    List<ProductsDTO> searchProducts(String query);

    /**
     * 판매자 ID로 상품 목록을 조회합니다.
     */
    List<Product> getProductsBySellerId(Long sellerId);

    /**
     * 특정 판매자가 사용 중인 카테고리를 조회합니다.
     */
    List<CategoryDTO> getUsedCategoriesBySeller(Long sellerId);

    /**
     * 일간 인기 상품을 조회합니다.
     */
    List<Product> getPopularProductsDaily(int page, int size);

    /**
     * 주간 인기 상품을 조회합니다.
     */
    List<Product> getPopularProductsWeekly();

    /**
     * 월간 인기 상품을 조회합니다.
     */
    List<Product> getPopularProductsMonthly();

    /**
     * 전체 인기 상품을 조회합니다.
     */
    List<Product> getAllPopularProducts();

    /**
     * 판매자별 일간 인기 상품을 조회합니다.
     */
    List<Product> getPopularProductsBySellerDaily(Long sellerId);

    /**
     * 판매자별 주간 인기 상품을 조회합니다.
     */
    List<Product> getPopularProductsBySellerWeekly(Long sellerId);

    /**
     * 판매자별 월간 인기 상품을 조회합니다.
     */
    List<Product> getPopularProductsBySellerMonthly(Long sellerId);

    /**
     * 상품 ID로 상세 조회합니다.
     */
    ProductDetailResponseDTO getProductDetail(Long productId);

    /**
     * 상품 상세 조회(예외 확인용 더미 메서드)
     */
    void getProductDetails(Long productId);
}
