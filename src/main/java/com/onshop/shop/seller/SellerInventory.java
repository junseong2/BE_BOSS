package com.onshop.shop.seller;

import java.time.LocalDateTime;

import org.hibernate.annotations.UpdateTimestamp;

import com.onshop.shop.products.Product;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity(name="Inventory")
@Table(name="Inventory")
@Data
@Builder
public class SellerInventory {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long inventoryId;
	
	private Long productId;
	
	private Long stock;
	
	@UpdateTimestamp
	private LocalDateTime updated_date;

}
