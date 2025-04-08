// ProductsEntity.java
package com.onshop.shop.product;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onshop.shop.category.Category;
import com.onshop.shop.seller.Seller;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
@Entity
@Table(name = "product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class) 
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
    
    @Column(name = "gImage", length = 10000)
    private String gImage;

    @Column(name="expiry_date")
    private LocalDateTime expiryDate;

    @Column(name="created_register")
    @CreatedDate
    private LocalDateTime createdRegister;
    

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    
    @Builder.Default
    @Column(name = "daily_sales")
    private Long dailySales = 0L;

    @Builder.Default
    @Column(name = "weekly_sales")
    private Long weeklySales = 0L;

    @Builder.Default
    @Column(name = "monthly_sales")
    private Long monthlySales = 0L;

    @Builder.Default
    @Column(name = "overall_sales")
    private Long overallSales = 0L;
    
    @Column(name = "origin_price")
    private Integer originPrice; // 원본 가격

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_rate")
    private DiscountRate discountRate; // 할인율

    @Builder.Default
    @Column(name = "is_discount")
    private Boolean isDiscount = false; // 할인 유무


    
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
    
    public void increaseSales(int quantity) {
        this.dailySales = (this.dailySales == null) ? 0L : this.dailySales;
        this.weeklySales = (this.weeklySales == null) ? 0L : this.weeklySales;
        this.monthlySales = (this.monthlySales == null) ? 0L : this.monthlySales;
        this.overallSales = (this.overallSales == null) ? 0L : this.overallSales;

        this.dailySales += quantity;
        this.weeklySales += quantity;
        this.monthlySales += quantity;
        this.overallSales += quantity;
        
        
    }
    
    // 기존 price 를 할인율을 적용한 가격으로 자동 변환 후 저장하는 메서드(isDiscount가 true인 경우에만 실행)
    public void applyDiscount() {
        if (Boolean.TRUE.equals(isDiscount) && discountRate != null && originPrice != null) {
            int rate = discountRate.getRate(); // 예: 20 (이 친구가 열거체 형태의 문자열 값을 정수형으로 변환해줌)
            this.price = originPrice - (originPrice * rate / 100); // 기존 price에 할인율을 적용한 가격으로 계산 후 변환
        } else if (originPrice != null) {
            this.price = originPrice; // 할인 없을 경우 원가 그대로
        }
    }
  
}