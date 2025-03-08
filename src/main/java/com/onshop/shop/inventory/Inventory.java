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
import lombok.Builder;
import lombok.Data;

@Entity(name="Inventory")
@Table(name="Inventory")
@Data
@Builder
public class Inventory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long inventoryId;

    private int stock;

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "product_id")  // productId 컬럼을 직접 쓰지 않고 객체로 참조해야 함
    private Product product;
    
    @UpdateTimestamp
	private LocalDateTime updated_date;

}
