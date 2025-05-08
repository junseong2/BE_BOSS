package com.onshop.shop.domain.inventory.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.UpdateTimestamp;

import com.onshop.shop.domain.product.entity.Product;
import com.onshop.shop.domain.seller.entity.Seller;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity(name="Inventory")
@Table(name="Inventory", uniqueConstraints = @UniqueConstraint(name = "UniqueInventoryIdAndProductId", columnNames = {"inventory_id", "product_id"}))
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    private Long stock;
    private Long minStock;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false) 
    @OnDelete(action = OnDeleteAction.CASCADE) // Product 삭제 시 관련 Inventory 삭제
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false) 
    @OnDelete(action = OnDeleteAction.CASCADE) // Seller 삭제 시 관련 Inventory 삭제
    private Seller seller;
    
    @UpdateTimestamp
	private LocalDateTime updated_date;
    
    
    // 최소 재고 보다 작은지 체크
    public boolean isBelowMinStock() {
    	return this.stock < this.minStock;
    }
    
    // 재고 증가
    public void increaseStock(Long quantity) {
        this.stock += quantity;
    }
    
    // 최소 재고 증가
    public void increaseMinStock(Long quantiy ) {
    	this.minStock += quantiy;
    }

}
