package com.onshop.shop.vector;

import java.util.List;
import com.onshop.shop.vector.ProductVectorDTO;


public interface ProductVectorService {

    List<ProductVectorDTO> searchVectorProducts(String query); // 검색

    ProductVectorDTO getVectorByProductId(Long productId); // 겟

    ProductVectorDTO saveVectorData(ProductVectorDTO vectorDTO); // 저장

    ProductVectorDTO updateVectorData(Long productId, ProductVectorDTO vectorDTO); // 업데이트

    void deleteVectorData(Long productId); // 삭제

    void syncProductVectors();  // MySQL → PostgreSQL 벡터 데이터 일괄 동기화
    
    void testEmbedProduct(Long productId);	// 임베딩 테스트용 - 하나 임베딩하고 로그 찍어주는 함수.
    
    List<Long> recommendProductsByRag(String query); // 코사인 유사도 띄워주는 함수.
}