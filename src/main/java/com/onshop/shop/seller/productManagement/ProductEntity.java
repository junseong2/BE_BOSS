package com.onshop.shop.seller.productManagement;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name="Products")
@Data
@Builder
public class ProductEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO )
	private Long productId;
	
	@JoinColumn
	@ManyToOne
	private Long categoryId;
	
	@JoinColumn
	@ManyToOne
	private Long sellerId;
	
	private String name;
	private String description;
	
	@DecimalMin(value="0.00")
	@Column(precision = 10, scale = 2)
	private BigDecimal price;
	private String expiredDate;
	
	@CreatedDate
	private LocalDateTime createdAt;
	
	}
