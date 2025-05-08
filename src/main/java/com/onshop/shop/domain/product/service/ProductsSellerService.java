package com.onshop.shop.domain.product.service;

import java.io.IOException;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.onshop.shop.domain.product.dto.SellerProductIdsDTO;
import com.onshop.shop.domain.product.dto.SellerProductsListDTO;
import com.onshop.shop.domain.product.dto.SellerProductsRequestDTO;
import com.onshop.shop.domain.product.dto.SellerProductsResponseDTO;
import com.onshop.shop.domain.product.entity.Product;
import com.opencsv.exceptions.CsvValidationException;

/**
 * 판매자 관점의 상품 관리 서비스 인터페이스
 */
public interface ProductsSellerService {

    /**
     * 대시보드에서 판매자의 전체 상품을 조회합니다.
     */
    SellerProductsResponseDTO getAllDashboardProducts(int page, int size, String search, Long userId);

    /**
     * 판매자 상품을 등록합니다 (다중 등록).
     */
    void registerProducts(List<SellerProductsRequestDTO> products, Long userId);

    /**
     * 판매자 상품을 삭제합니다.
     */
    void removeProducts(SellerProductIdsDTO productsIds, Long userId);

    /**
     * 판매자 상품 정보를 수정합니다.
     */
    Product updateProducts(Long productId, SellerProductsRequestDTO product, Long userId);

    /**
     * 판매자 상품을 등록합니다 (단일 등록).
     */
    Product registerProduct(SellerProductsRequestDTO product, Long userId);

    /**
     * 상품 이미지 목록을 업로드합니다.
     */
    void registerProductImages(List<MultipartFile> images, Product product);

    /**
     * 판매자별 상품 목록 조회 (페이징, 정렬, 카테고리 포함).
     */
    SellerProductsResponseDTO getAllProducts(Long sellerId, int page, int size, String search, String sort, Long categoryId);

    /**
     * 상품 전체 조회 (관리자 또는 통합용).
     */
    SellerProductsResponseDTO getAllProducts(int page, int size, String search, String sort);

    /**
     * 판매자별 상품 조회 (카테고리 제외).
     */
    SellerProductsResponseDTO getAllProducts(Long sellerId, int page, int size, String search, String sort);

    /**
     * 페이징 처리된 상품 목록 조회 (내부용).
     */
    Page<Product> getAllProductsPage(Long sellerId, int page, int size);

    /**
     * 판매자별 상품을 페이징으로 조회합니다.
     */
    Page<Product> getProductsBySeller(Long sellerId, Pageable pageable);

    /**
     * 판매자 상품 전체를 정렬 및 필터 조건으로 조회합니다.
     */
    List<SellerProductsListDTO> getAllSellerProducts(Long sellerId, int page, int size, String search, String sort, Long categoryId);

    /**
     * 판매자 상품 CSV 업로드 처리
     */
    void uploadProductsCsv(MultipartFile file, Long userId) throws IOException, CsvValidationException;
}
