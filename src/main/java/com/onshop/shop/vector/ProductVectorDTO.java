package com.onshop.shop.vector;

import com.onshop.shop.product.Product;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.postgresql.util.PGobject;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVectorDTO {

    private Long productId;
    private Long categoryId;
    private Integer price;
    private String categoryName;

    // ✅ MySQL Product 테이블에서 가져온 정보 (임베딩용)
    private String productName;
    private String productDescription;

    // ✅ PostgreSQL에 저장될 벡터 데이터 (JSON 문자열 형태)
    private String productEmbedding;

    /**
     * ✅ OpenAI 임베딩용 텍스트 생성
     */
    @JsonIgnore
    public String toEmbeddingText() {
        StringBuilder sb = new StringBuilder();
        sb.append(categoryName != null ? categoryName + " " : "");
        sb.append(productName != null ? productName + " " : "");
        sb.append(productDescription != null ? productDescription : "");
        return sb.toString().trim();
    }

    /**
     * ✅ float[] → JSON(String) 변환 (저장용)
     */
    public void setEmbeddingFromArray(float[] embeddingArray) {
        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < embeddingArray.length; i++) {
            sb.append(embeddingArray[i]);
            if (i < embeddingArray.length - 1) sb.append(", ");
        }
        sb.append(")");
        this.productEmbedding = sb.toString();
    }

    /**
     * ✅ JSON(String) → float[] 변환 (검색용)
     */
    public float[] getEmbeddingAsArray() {
        if (productEmbedding == null) return null;

        String[] tokens = productEmbedding.replace("(", "").replace(")", "").split(",");
        float[] result = new float[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            result[i] = Float.parseFloat(tokens[i].trim());
        }
        return result;
    }

    /**
     * ✅ DTO → Entity 변환
     */
    public ProductVector toEntity() {
        ProductVector entity = ProductVector.builder()
                .productId(this.productId)
                .categoryId(this.categoryId)
                .price(this.price)
                .build();

        entity.setEmbeddingFromFloatArray(this.getEmbeddingAsArray());
        return entity;
    }

    /**
     * ✅ Entity → DTO 변환
     */
    public static ProductVectorDTO fromEntity(ProductVector entity) {
        ProductVectorDTO dto = ProductVectorDTO.builder()
                .productId(entity.getProductId())
                .categoryId(entity.getCategoryId())
                .price(entity.getPrice())
                .build();

        // PGobject → float[] → 벡터 문자열로
        try {
            String pgValue = entity.getProductEmbedding().getValue();
            dto.setProductEmbedding(pgValue); // 그냥 문자열 형태로 저장
        } catch (Exception e) {
            throw new RuntimeException("PGobject에서 문자열 추출 실패", e);
        }

        return dto;
    }

    public static ProductVectorDTO fromProduct(Product product) {
        return ProductVectorDTO.builder()
                .productId(product.getProductId())
                .categoryId(product.getCategory().getId())  // 카테고리 ID
                .productName(product.getName())             // 상품명
                .productDescription(product.getDescription()) // 설명
                .categoryName(product.getCategory().getName()) // ✅ 카테고리명
                .price(product.getPrice())
                .build();
    }
}
