package com.onshop.shop.vector;

import com.onshop.shop.product.Product;
import com.onshop.shop.product.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductVectorServiceImpl implements ProductVectorService {

    private final OpenAIEmbeddingService openAIEmbeddingService;
    private final ProductVectorRepository productVectorRepository;
    private final ProductRepository productRepository;
    
    private static class ScoredProduct {
        ProductVector vector;
        double similarity;

        public ScoredProduct(ProductVector vector, double similarity) {
            this.vector = vector;
            this.similarity = similarity;
        }
    }

    @Override
    public List<ProductVectorDTO> searchVectorProducts(String query) {
        log.info("🔍 벡터 검색 요청: {}", query);
        return List.of(); // 추후 검색 로직 구현
    }

    @Override
    public ProductVectorDTO getVectorByProductId(Long productId) {
        log.info("📄 벡터 데이터 조회 요청: {}", productId);
        Optional<ProductVector> vectorEntity = productVectorRepository.findById(productId);
        return vectorEntity.map(ProductVectorDTO::fromEntity).orElse(null);
    }

    @Override
    public ProductVectorDTO saveVectorData(ProductVectorDTO vectorDTO) {
        log.info("📝 벡터 데이터 저장 요청: {}", vectorDTO);
        ProductVector savedEntity = productVectorRepository.save(vectorDTO.toEntity());
        return ProductVectorDTO.fromEntity(savedEntity);
    }

    @Override
    public ProductVectorDTO updateVectorData(Long productId, ProductVectorDTO vectorDTO) {
        log.info("🔄 벡터 데이터 수정 요청: {}", productId);
        if (productVectorRepository.existsById(productId)) {
            ProductVector updatedEntity = productVectorRepository.save(vectorDTO.toEntity());
            return ProductVectorDTO.fromEntity(updatedEntity);
        }
        return null;
    }

    @Override
    public void deleteVectorData(Long productId) {
        log.info("🗑️ 벡터 데이터 삭제 요청: {}", productId);
        productVectorRepository.deleteById(productId);
    }

    @Override
    public void syncProductVectors() {
        log.info("🔄 MySQL → PGVector 벡터 데이터 동기화 시작");

        List<Product> productList = productRepository.findAll();
        log.info("📦 총 {}개의 상품 데이터 조회됨", productList.size());

        int skipped = 0, success = 0, failed = 0;

        for (Product product : productList) {
            Long productId = product.getProductId();

            if (productVectorRepository.existsById(productId)) {
                log.info("⏭️ 이미 임베딩된 상품: productId={}", productId);
                skipped++;
                continue;
            }

            try {
                ProductVectorDTO dto = ProductVectorDTO.fromProduct(product);
                String embeddingText = dto.toEmbeddingText();

                float[] embeddingArray = openAIEmbeddingService.getEmbedding(embeddingText);
                String vectorString = PGVectorAttributeConverter.toPgVectorString(embeddingArray);

                productVectorRepository.insertVectorWithTextCast(
                        productId,
                        product.getCategory().getId(),
                        product.getPrice(),
                        vectorString
                );

                log.info("✅ 임베딩 및 저장 완료: productId={}", productId);
                success++;

            } catch (RuntimeException e) {
                log.warn("❌ 상품 [{}] 임베딩 실패: {}", productId, e.getMessage());
                failed++;
            }
        }

        log.info("✅ PGVector 동기화 완료 — 총: {}, 성공: {}, 건너뜀: {}, 실패: {}", productList.size(), success, skipped, failed);
    }

    @Override
    public void testEmbedProduct(Long productId) {
        log.info("🧪 [TEST] 임베딩 시작: productId={}", productId);
        Optional<Product> productOpt = productRepository.findById(productId);

        if (productOpt.isEmpty()) {
            log.warn("❌ [TEST] 해당 productId={} 의 상품이 존재하지 않음", productId);
            return;
        }

        try {
            Product product = productOpt.get();
            ProductVectorDTO dto = ProductVectorDTO.fromProduct(product);
            String embeddingText = dto.toEmbeddingText();

            float[] embeddingArray = openAIEmbeddingService.getEmbedding(embeddingText);
            String vectorString = PGVectorAttributeConverter.toPgVectorString(embeddingArray);

            productVectorRepository.insertVectorWithTextCast(
                    product.getProductId(),
                    product.getCategory().getId(),
                    product.getPrice(),
                    vectorString
            );

            log.info("✅ [TEST] 임베딩 및 저장 성공: productId={}", productId);
        } catch (RuntimeException e) {
            log.error("❌ [TEST] 임베딩 실패: {}", e.getMessage(), e);
        }
    }

    @Override
    
    // 단백질 건강 3 50000 3000
    public List<Long> recommendProductsByRag(String query) {
        log.info("🧠 [RAG] 유사도 기반 상품 추천 시작: {}", query);

        // 1. 쿼리 토큰 분리
        String[] tokens = query.trim().split(" ");
        int n = tokens.length;

        int recommendationCount = 3;
        int priceAvg = 0;
        int priceStdDev = 0;

        int numbersParsed = 0;
        for (int i = n - 1; i >= 0 && numbersParsed < 3; i--) {
            if (tokens[i].matches("\\d+")) {
                int num = Integer.parseInt(tokens[i]);
                switch (numbersParsed) {
                    case 0 -> priceStdDev = num;
                    case 1 -> priceAvg = num;
                    case 2 -> recommendationCount = num;
                }
                numbersParsed++;
            } else {
                break;
            }
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < n - numbersParsed; i++) {
            sb.append(tokens[i]).append(" ");
        }
        String textOnly = sb.toString().trim();

        log.info("🧾 [쿼리 파싱] 키워드='{}', 추천개수={}, 평균={}, 표준편차={}", textOnly, recommendationCount, priceAvg, priceStdDev);

        float[] queryEmbedding;
        try {
            queryEmbedding = openAIEmbeddingService.getEmbedding(textOnly);
        } catch (Exception e) {
            log.error("❌ [RAG] 임베딩 실패: {}", e.getMessage(), e);
            return List.of();
        }

        String queryEmbeddingStr = PGVectorAttributeConverter.toPgVectorString(queryEmbedding);

        List<Object[]> rows = productVectorRepository.findTopBySimilarity(queryEmbeddingStr, recommendationCount * 5);
        log.info("📊 [RAG] 유사도 Top {}개 벡터 조회 완료", rows.size());

        if (rows.isEmpty()) {
            log.warn("⚠️ [RAG] 유사도 기반 추천 결과 없음");
            return List.of();
        }

        // ✅ 여기서부터 변경: DTO 변환 대신 productId만 추출
        return rows.stream()
                .map(row -> ((Number) row[0]).longValue()) // row[0] = product_id
                .toList();
    }
    
    
    private double cosineSimilarity(float[] a, float[] b) {
        double dot = 0.0, normA = 0.0, normB = 0.0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}





/* -- 

기존방식은 모든 임베딩을 불러와 jvm위에서 코사인 유사도를 검색하는 로직이었음
현재 pg벡터내의 쿼리문을 이용해서 코사인 유사도 상위 n개의 데이터만 받아오도록 처리해둠 

@Override
public List<ProductVectorDTO> recommendProductsByRag(String query) {
    log.info("🧠 [RAG] 유사도 기반 상품 추천 시작: {}", query);

    // 1. 키워드 텍스트만 추출
    String[] tokens = query.split(" ");
    StringBuilder sb = new StringBuilder();
    for (String token : tokens) {
        if (!token.matches("\\d+")) {
            sb.append(token).append(" ");
        }
    }
    String textOnly = sb.toString().trim();

    // 2. OpenAI 임베딩 생성
    float[] queryEmbedding;
    try {
        queryEmbedding = openAIEmbeddingService.getEmbedding(textOnly);
    } catch (Exception e) {
        log.error("❌ [RAG] 임베딩 실패: {}", e.getMessage(), e);
        return List.of();
    }

    // 3. 전체 벡터 + 임베딩 문자열 직접 조회 (Native Query)
    List<Object[]> rows = productVectorRepository.findAllWithEmbeddingsRaw();
    log.info("📊 [RAG] 임베딩 포함 벡터 개수: {}", rows.size());
    if (rows.isEmpty()) {
        log.warn("⚠️ [RAG] 저장된 벡터가 없음");
        return List.of();
    }

    // 4. 유사도 계산 및 상위 3개 추출
    List<ScoredProduct> scoredProducts = rows.stream().map(row -> {
        try {
            Long productId = ((Number) row[0]).longValue();
            Long categoryId = ((Number) row[1]).longValue();
            Integer price = ((Number) row[2]).intValue();
            PGobject pgObj1 = (PGobject) row[3]; // ✅ 실제 타입은 PGobject
            String vectorStr = pgObj1.getValue().replace('[', '(').replace(']', ')');
            float[] vector = PGVectorAttributeConverter.fromPgVectorString(vectorStr);

            
            double similarity = cosineSimilarity(queryEmbedding, vector);

            PGobject pgObj = new PGobject();
            pgObj.setType("vector");
            pgObj.setValue(vectorStr);

            ProductVector pv = ProductVector.builder()
                .productId(productId)
                .categoryId(categoryId)
                .price(price)
                .productEmbedding(pgObj)
                .build();

            return new ScoredProduct(pv, similarity);
        } catch (Exception e) {
            log.warn("⚠️ [RAG] 유사도 계산 중 오류 (row): {}", e.getMessage());
            return null;
        }
    }).filter(sp -> sp != null)
      .sorted((a, b) -> Double.compare(b.similarity, a.similarity))
      .limit(3)
      .toList();

    // 5. DTO 변환 및 로깅
    return scoredProducts.stream()
        .map(sp -> {
            ProductVectorDTO dto = ProductVectorDTO.fromEntity(sp.vector);
            log.info("🎯 추천 상품: productId={}, 상품명={}, similarity={}",
                    dto.getProductId(), dto.getProductName(), sp.similarity);
            return dto;
        })
        .toList();
}
*/