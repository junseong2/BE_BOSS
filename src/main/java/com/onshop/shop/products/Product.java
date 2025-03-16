// ProductsEntity.java
package com.onshop.shop.products;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import com.onshop.shop.category.Category;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name="seller_id")
    private Long sellerId;

    @Column(name="name", nullable = false)
    private String name;

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


}