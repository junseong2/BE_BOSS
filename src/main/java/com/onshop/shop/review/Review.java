package com.onshop.shop.review;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.onshop.shop.product.Product;
import com.onshop.shop.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class) 
public class Review {
	
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reviewId;
    
    @ManyToOne
    @JoinColumn(name = "user_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User user;
    
    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    private Integer rating;  // 별점 
    private String reviewText;  // 리뷰 
    
    private Boolean isAnswered; //리뷰 답변 유무
    private String gImages;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 생성일시

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;  // 수정일시

    // 이미지를 배열 형태로 반환
    public List<String> getImageList() {
        return (this.gImages != null && !this.gImages.isEmpty())
                ? Arrays.asList(this.gImages.split(","))
                : List.of();
    }
}
