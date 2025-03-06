package com.onshop.shop.seller.inventoryManagement;

import java.time.LocalDateTime;

import org.hibernate.annotations.Check;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name="inventory")
@Data
public class InventoryEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long inventoryId;
	
	private Long productId;
	
	private Long stock;
	
	@UpdateTimestamp
	private LocalDateTime updatedDate;

}
