package com.onshop.shop.domain.vector.entity;


import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.postgresql.util.PGobject;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "product_vector")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVector {

    @Id
    @Column(name = "product_id")
    private Long productId;  // MySQL의 Product 테이블과 연동되는 상품 ID

    @Column(name = "category_id", nullable = false)
    private Long categoryId; // MySQL Category 테이블의 ID 참조

    @Transient // JPA가 이 필드를 DB랑 매핑하지 않게 함
    private PGobject productEmbedding;

    @Column(name = "price", nullable = false)
    private Integer price;  // 상품 가격

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;  // 생성일시 (자동 저장)

    public void setEmbeddingFromFloatArray(float[] vector) {
        if (vector == null) {
            this.productEmbedding = null;
            return;
        }

        StringBuilder sb = new StringBuilder("(");
        for (int i = 0; i < vector.length; i++) {
            sb.append(vector[i]);
            if (i < vector.length - 1) sb.append(", ");
        }
        sb.append(")");

        PGobject pg = new PGobject();
        try {
            pg.setType("vector");
            pg.setValue(sb.toString());
            this.productEmbedding = pg;
        } catch (Exception e) {
            throw new RuntimeException("PGVector 변환 실패", e);
        }
    }

    @Override
    public String toString() {
        return "ProductVector{" +
                "productId=" + productId +
                ", categoryId=" + categoryId +
                ", price=" + price +
                ", createdAt=" + createdAt +
                '}';
    }
}