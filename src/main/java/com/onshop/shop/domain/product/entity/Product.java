package com.onshop.shop.domain.product.entity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onshop.shop.domain.category.entity.Category;
import com.onshop.shop.domain.product.enums.DiscountRate;
import com.onshop.shop.domain.seller.entity.Seller;

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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private Long productId;

    // 카테고리와 다대일 관계
//    @ManyToOne(fetch = FetchType.EAGER)
    @ManyToOne(fetch = FetchType.LAZY)
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
    
    @Lob
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    private Integer price;
    private Integer originPrice; // 원본 가격
    
    @Column(name = "g_image", length = 10000)
    private String gImage;



    private LocalDateTime expiryDate;

    @CreatedDate
    private LocalDateTime createdRegister;
    

    @Builder.Default
    @Column(nullable = false)
    private Long viewCount = 0L;

    
    @Builder.Default
    private Long dailySales = 0L;

    @Builder.Default
    private Long weeklySales = 0L;

    @Builder.Default
    private Long monthlySales = 0L;

    @Builder.Default
    private Long overallSales = 0L;
    

    @Enumerated(EnumType.STRING)
    private DiscountRate discountRate; // 할인율

    @Builder.Default
    private Boolean isDiscount = false; // 할인 유무

    
    // 추가된 생성자
    public Product(Long productId, Category category, Seller seller, String name, String description,
                   Integer price, String gImage, LocalDateTime expiryDate, LocalDateTime createdRegister,
                   Long viewCount, Long dailySales, Long weeklySales, Long monthlySales, Long overallSales,
                   Integer originPrice, DiscountRate discountRate, Boolean isDiscount) {
        this.productId = productId;
        this.category = category;
        this.seller = seller;
        this.name = name;
        this.description = description;
        this.price = price;
        this.gImage = gImage;
        this.expiryDate = expiryDate;
        this.createdRegister = createdRegister;
        this.viewCount = viewCount;
        this.dailySales = dailySales;
        this.weeklySales = weeklySales;
        this.monthlySales = monthlySales;
        this.overallSales = overallSales;
        this.originPrice = originPrice;
        this.discountRate = discountRate;
        this.isDiscount = isDiscount;
    }
    
    // 쉼표(,)로 구분된 gImages를 리스트로 변환하여 반환
    public List<String> getImageList() {
        String s3BaseUrl = "https://bossassets.s3.amazonaws.com/";

        return (this.gImage != null && !this.gImage.isEmpty())
                ? Arrays.stream(this.gImage.split(","))
                        .map(String::trim)
                        .map(fileName -> s3BaseUrl + fileName)
                        .collect(Collectors.toList())
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