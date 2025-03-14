// ProductsEntity.java
package com.onshop.shop.products;

import java.math.BigDecimal;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import org.springframework.data.annotation.CreatedDate;

import com.onshop.shop.category.Category;

@Entity
@Table(name = "product")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="product_id")
    private Long productId;

    // 카테고리와 다대일 관계
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    private Category category;

    
    
    
    @Column(name="seller_id")
    private Long sellerId;

    @Column(name="name", nullable = false)
    private String name;
    @Column(name="store_id", nullable = false)  // ✅ storeId 추가
    private Long storeId;
    @Column(name="description")
    private String description;

    @Column(name="price")
    private Integer price;
    
    @Column(name="gImage")
    private String gImage;

    @Column(name="expiry_date")
    private LocalDateTime expiryDate;

    @Column(name="created_register")
    private LocalDateTime createdRegister;
    
    // ✅ 쉼표(,)로 구분된 gImages를 리스트로 변환하여 반환
    public List<String> getImageList() {
        return (this.gImage != null && !this.gImage.isEmpty())
                ? Arrays.asList(this.gImage.split(","))
                : List.of();
    }
    public Long getProductId() {
        return productId;
    }

    public String getName() {
        return name;
    }
    
}