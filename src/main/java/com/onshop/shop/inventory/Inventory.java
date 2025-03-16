package com.onshop.shop.inventory;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.onshop.shop.products.Product;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
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

    @OneToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")  // productId 컬럼을 직접 쓰지 않고 객체로 참조해야 함
    private Product product;

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
