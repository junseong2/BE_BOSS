package com.onshop.shop.domain.vector.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.onshop.shop.domain.vector.entity.ProductVector;

/**
 *  PostgreSQL product_vector 테이블과 연동되는 Repository
 */
@Repository
public interface ProductVectorRepository extends JpaRepository<ProductVector, Long> {

    /**
     * PGVector 삽입 또는 업데이트 (임베딩 저장용)
     */
    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO product_vector (product_id, category_id, price, product_embedding)
        VALUES (:productId, :categoryId, :price, CAST(:vectorStr AS vector))
        ON CONFLICT (product_id) DO UPDATE
        SET category_id = EXCLUDED.category_id,
            price = EXCLUDED.price,
            product_embedding = EXCLUDED.product_embedding
        """, nativeQuery = true)
    void insertVectorWithTextCast(
            @Param("productId") Long productId,
            @Param("categoryId") Long categoryId,
            @Param("price") Integer price,
            @Param("vectorStr") String vectorStr
    );

    /**
     * ⚠️ [LEGACY] 모든 product_embedding을 불러오는 구 방식 (코사인 계산을 Java에서 수행)
     * → 대량 데이터 처리 시 성능 이슈 발생
     */
    @Deprecated
    @Query(value = """
        SELECT product_id, category_id, price, product_embedding
        FROM product_vector
        WHERE product_embedding IS NOT NULL
        """, nativeQuery = true)
    List<Object[]> findAllWithEmbeddingsRaw();

    /**
     * [신규] OpenAI 쿼리 임베딩과 유사한 벡터 상위 N개만 가져오기 (코사인 유사도 기준)
     * DB 내부에서 유사도 정렬 → Java에서 코사인 계산 필요 없음
     */
    @Query(value = """
        SELECT product_id, category_id, price, product_embedding
        FROM product_vector
        WHERE product_embedding IS NOT NULL
        ORDER BY product_embedding <-> CAST(:queryEmbedding AS vector)
        LIMIT :topCount
        """, nativeQuery = true)
    List<Object[]> findTopBySimilarity(
            @Param("queryEmbedding") String queryEmbedding,
            @Param("topCount") int topCount
    );
}