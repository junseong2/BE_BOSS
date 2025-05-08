package com.onshop.shop.domain.settlement.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.onshop.shop.domain.seller.entity.Seller;
import com.onshop.shop.domain.settlement.enums.SettlementStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Data
@Builder
@NoArgsConstructor  
@AllArgsConstructor 
@EntityListeners(AuditingEntityListener.class) 
public class Settlement {
   
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;
   
    @ManyToOne
    @JoinColumn(name = "seller_id", nullable = false) 
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Seller seller;
   
   @Enumerated(EnumType.STRING)
    private SettlementStatus status;
   
    private Long requestedAmount; 
    private String bankName;         // 은행 이름
    private String accountNumber;    // 계좌 번호
    private String accountHolder;    // 예금주 이름

    @CreatedDate
    private LocalDateTime createdDate;  // 생성일
    @LastModifiedDate
    private LocalDateTime updatedDate;  // 수정일

}
