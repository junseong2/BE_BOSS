package com.onshop.shop.review;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.onshop.shop.product.Product;
import com.onshop.shop.seller.Seller;
import com.onshop.shop.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.Builder;
import lombok.Data;

@Entity
@Builder
@Data
@EntityListeners(AuditingEntityListener.class) 
public class ReviewAnswer {
	

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long answerId;
    
    @OneToOne
    @JoinColumn(name = "review_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Review review;
    
    private String answerText;  // 응답
    
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) 
    private Seller seller;
   
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;  // 생성일시

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime lastModifiedAt;  // 수정일시

}
