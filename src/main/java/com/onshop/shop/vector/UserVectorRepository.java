package com.onshop.shop.vector;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface UserVectorRepository extends JpaRepository<UserVector, Long> {

    /**
     * ✅ user_id로 단건 조회 (PGVector 필드 매핑 회피용)
     */
    @Query(value = """
        SELECT user_id, update_count, product_ids
        FROM user_vector
        WHERE user_id = :userId
        """, nativeQuery = true)
    Optional<UserVector> findByUserId(@Param("userId") Long userId);

    /**
     * ✅ user_id로 raw 벡터 포함 조회
     */
    @Query(value = """
        SELECT user_id, update_count, product_ids,
               avg_vector::text AS avg_vector,
               recent_vector::text AS recent_vector
        FROM user_vector
        WHERE user_id = :userId
        """, nativeQuery = true)
    Map<String, Object> findRawByUserId(@Param("userId") Long userId);

    /**
     * ✅ user_vector 삽입 또는 업데이트 (벡터 포함)
     */
    @Transactional
    @Modifying(clearAutomatically = true)
    @Query(value = """
        INSERT INTO user_vector (user_id, update_count, avg_vector, recent_vector, product_ids)
        VALUES (:userId, :updateCount, CAST(:avgVectorStr AS vector), CAST(:recentVectorStr AS vector), :productIds)
        ON CONFLICT (user_id) DO UPDATE
        SET update_count = :updateCount,
            avg_vector = CAST(:avgVectorStr AS vector),
            recent_vector = CAST(:recentVectorStr AS vector),
            product_ids = :productIds
        """, nativeQuery = true)
    void upsertUserVector(
        @Param("userId") Long userId,
        @Param("updateCount") Long updateCount,
        @Param("avgVectorStr") String avgVectorStr,
        @Param("recentVectorStr") String recentVectorStr,
        @Param("productIds") Long[] productIds
    );

    /**
     * ✅ 존재하지 않을 경우 기본값으로 삽입 (0벡터 + 빈 배열)
     */
    @Transactional
    @Modifying
    @Query(value = """
        INSERT INTO user_vector (user_id, update_count, avg_vector, recent_vector, product_ids)
        SELECT :userId,
               0,
               CAST(:zeroVectorStr AS vector),
               CAST(:zeroVectorStr AS vector),
               ARRAY[]::BIGINT[]
        WHERE NOT EXISTS (
            SELECT 1 FROM user_vector WHERE user_id = :userId
        )
        """, nativeQuery = true)
    void insertIfNotExistsWithVectors(
        @Param("userId") Long userId,
        @Param("zeroVectorStr") String zeroVectorStr
    );

    /**
     * ✅ avg_vector 기준 유사 사용자 조회
     */
    @Query(value = """
        SELECT user_id, product_ids
        FROM user_vector
        WHERE user_id != :userId
        ORDER BY avg_vector <-> CAST(:queryVector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopUsersByAvgVectorSimilarity(
        @Param("userId") Long userId,
        @Param("queryVector") String queryVector,
        @Param("limit") int limit
    );

    /**
     * ✅ recent_vector 기준 유사 사용자 조회
     */
    @Query(value = """
        SELECT user_id, product_ids
        FROM user_vector
        WHERE user_id != :userId
        ORDER BY recent_vector <-> CAST(:queryVector AS vector)
        LIMIT :limit
        """, nativeQuery = true)
    List<Object[]> findTopUsersByRecentVectorSimilarity(
        @Param("userId") Long userId,
        @Param("queryVector") String queryVector,
        @Param("limit") int limit
    );
}
