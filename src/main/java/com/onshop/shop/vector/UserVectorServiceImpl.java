package com.onshop.shop.vector;

import com.onshop.shop.product.Product;
import com.onshop.shop.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;

import java.sql.Array;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserVectorServiceImpl implements UserVectorService {

    private final UserVectorRepository userVectorRepository;
    private final ProductRepository productRepository;
    private final OpenAIEmbeddingService openAIEmbeddingService;

    @Override
    public void handleAddToCartAndUpdateUserVector(Long userId, Long productId) {
        log.info("🛒 장바구니 추가 요청 도착: userId={}, productId={}", userId, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다."));
        String productText = product.getName() + " " + product.getDescription();
        float[] newEmbedding = openAIEmbeddingService.getEmbedding(productText);

        // ✅ 0벡터 스트링 생성
        String zeroVectorStr = PGVectorAttributeConverter.toPgVectorString(new float[1536]);

        // ✅ 없으면 기본값으로 삽입
        userVectorRepository.insertIfNotExistsWithVectors(userId, zeroVectorStr);

        // ✅ 조회 (벡터 포함)
        Map<String, Object> raw = userVectorRepository.findRawByUserId(userId);

        float[] avg = PGVectorAttributeConverter.fromPgVectorString((String) raw.get("avg_vector"));
        float[] recent = PGVectorAttributeConverter.fromPgVectorString((String) raw.get("recent_vector"));
        Long[] productIdsArr = (Long[]) raw.get("product_ids");
        if (productIdsArr == null) productIdsArr = new Long[0];

        Long updateCount = (Long) raw.get("update_count");
        if (updateCount == null) updateCount = 0L;

        // ✅ 평균 벡터 업데이트
        for (int i = 0; i < 1536; i++) {
            avg[i] = (updateCount / (float)(updateCount + 1)) * avg[i]
                   + (1 / (float)(updateCount + 1)) * newEmbedding[i];
        }

        // ✅ 최근 벡터 업데이트
        float weight = 0.5f;
        for (int i = 0; i < 1536; i++) {
            recent[i] = (1 - weight) * recent[i] + weight * newEmbedding[i];
        }

        // ✅ 상품 ID 업데이트 (최대 20개 유지)
        Set<Long> updatedProducts = new LinkedHashSet<>(Arrays.asList(productIdsArr));
        updatedProducts.remove(productId);
        updatedProducts.add(productId);
        List<Long> limitedList = new ArrayList<>(updatedProducts);
        if (limitedList.size() > 20) {
            limitedList = limitedList.subList(limitedList.size() - 20, limitedList.size());
        }
        Long[] productIds = limitedList.toArray(new Long[0]);

        // ✅ DB 저장용 문자열 변환
        String avgVectorStr = PGVectorAttributeConverter.toPgVectorString(avg);
        String recentVectorStr = PGVectorAttributeConverter.toPgVectorString(recent);

        // ✅ upsert
        userVectorRepository.upsertUserVector(
                userId,
                updateCount + 1,
                avgVectorStr,
                recentVectorStr,
                productIds
        );

        log.info("✅ user_id={}의 user_vector 갱신 완료 (최근 상품 {}개)", userId, productIds.length);
    }


    @Override
    public List<Long> recommendProducts(Long userId, int topK, int neighborCount) {
        log.info("🔍 추천 요청 → userId={}, topK={}, neighborCount={}", userId, topK, neighborCount);

        Map<String, Object> raw = userVectorRepository.findRawByUserId(userId);
        if (raw == null || raw.isEmpty()) {
            throw new RuntimeException("❌ user_vector 없음: userId=" + userId);
        }

        float[] avg = PGVectorAttributeConverter.fromPgVectorString((String) raw.get("avg_vector"));
        float[] recent = PGVectorAttributeConverter.fromPgVectorString((String) raw.get("recent_vector"));

        Set<Long> myProductSet;
        Object[] objectArray = (Object[]) raw.get("product_ids");
        if (objectArray != null) {
            myProductSet = Arrays.stream(objectArray)
                    .filter(Objects::nonNull)
                    .map(obj -> ((Number) obj).longValue())
                    .collect(Collectors.toSet());
        } else {
            myProductSet = new HashSet<>();
        }
        log.info("📦 사용자 담은 상품 ID 목록 ({}개): {}", myProductSet.size(), myProductSet);

        String avgVectorStr = PGVectorAttributeConverter.toPgVectorString(avg);
        String recentVectorStr = PGVectorAttributeConverter.toPgVectorString(recent);

        List<Object[]> avgUsers = userVectorRepository.findTopUsersByAvgVectorSimilarity(userId, avgVectorStr, neighborCount);
        List<Object[]> recentUsers = userVectorRepository.findTopUsersByRecentVectorSimilarity(userId, recentVectorStr, neighborCount);

        Set<Long> candidateSet = new LinkedHashSet<>();

        for (Object[] row : avgUsers) {
            Long otherUserId = ((Number) row[0]).longValue();
            if (Objects.equals(otherUserId, userId)) continue;

            Object rawArr = row[1];
            log.info("📊 avgUser[{}] product_ids class: {}", otherUserId, rawArr == null ? "null" : rawArr.getClass().getName());

            if (rawArr instanceof Object[] pidObjs) {
                for (Object obj : pidObjs) {
                    if (obj != null) candidateSet.add(((Number) obj).longValue());
                }
            }
        }

        for (Object[] row : recentUsers) {
            Long otherUserId = ((Number) row[0]).longValue();
            if (Objects.equals(otherUserId, userId)) continue;

            Object rawArr = row[1];
            log.info("📊 recentUser[{}] product_ids class: {}", otherUserId, rawArr == null ? "null" : rawArr.getClass().getName());

            if (rawArr instanceof Object[] pidObjs) {
                for (Object obj : pidObjs) {
                    if (obj != null) candidateSet.add(((Number) obj).longValue());
                }
            }
        }

        log.info("🧪 candidateSet (raw): {}", candidateSet);
        candidateSet.removeAll(myProductSet);
        log.info("🧼 제거 후 candidateSet: {}", candidateSet);

        Set<Long> alreadySeen = new HashSet<>(myProductSet);
        alreadySeen.addAll(candidateSet);

        if (candidateSet.size() < topK) {
            int needed = topK - candidateSet.size();
            log.info("⚠️ 후보 부족: 랜덤 상품 {}개 보충", needed);

            int totalCount = (int) productRepository.count();
            int attempt = 0, maxAttempt = 10;

            while (candidateSet.size() < topK && attempt++ < maxAttempt) {
                int randomPage = new Random().nextInt(Math.max(1, totalCount / Math.max(1, topK)));
                PageRequest pageable = PageRequest.of(randomPage, topK);
                List<Long> randomIds = productRepository.findRandomProductIds(pageable).getContent();

                for (Long id : randomIds) {
                    if (!alreadySeen.contains(id)) {
                        candidateSet.add(id);
                        alreadySeen.add(id);
                    }
                    if (candidateSet.size() >= topK) break;
                }
            }
        }

        List<Long> result = new ArrayList<>(candidateSet);
        if (result.size() > topK) result = result.subList(0, topK);

        log.info("✅ 최종 추천 상품 수: {}", result.size());
        return result;
    }

}