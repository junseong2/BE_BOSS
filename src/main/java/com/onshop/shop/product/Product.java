// ProductsEntity.java
package com.onshop.shop.product;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onshop.shop.category.Category;
import com.onshop.shop.seller.Seller;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id")
    @OnDelete(action = OnDeleteAction.NO_ACTION) // Seller 삭제 시 관련 Inventory 삭제
    private Category category;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false) 
    @OnDelete(action = OnDeleteAction.CASCADE) // Seller 삭제 시 관련 Inventory 삭제
    private Seller seller;

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
    
    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;
    
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
    
    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", price=" + price +
                ", gImage='" + gImage + '\'' +
                ", expiryDate=" + expiryDate +
                '}';
    }
}