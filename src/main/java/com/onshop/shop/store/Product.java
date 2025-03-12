package com.onshop.shop.store;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Data
@Entity(name = "ProductEntity2")  // 엔티티 이름을 다르게 지정
@Table(name = "product")  // 실제 테이블 이름 매핑

	public class Product {
	    @Id
	    @GeneratedValue(strategy = GenerationType.IDENTITY)
	    @Column(name = "product_id")
	    private Long id;  // 상품 고유 ID

	    @Column(name = "category_id", nullable = false)
	    private Long categoryId;  // 카테고리 ID

	    @ManyToOne
	    @JoinColumn(name = "seller_id", referencedColumnName = "seller_id", nullable = false)  // 외래 키 참조
	    private Seller seller;  // 판매자 정보

	    @Column(name = "name", nullable = false, length = 100)
	    private String name;  // 상품 이름

	    @Column(name = "description", columnDefinition = "TEXT")
	    private String description;  // 상품 설명


	    @Column(name="price")
	    private Integer price;

	    @Column(name = "expiry_date", columnDefinition = "DATE")
	    private LocalDateTime expiryDate;  // 만료일

	    @Column(name = "created_register", columnDefinition = "TIMESTAMP")
	    private LocalDateTime createdRegister;  // 상품 등록일

	    public Product() {}

	    public Product(Long categoryId, Seller seller, String name, String description, Integer price, LocalDateTime expiryDate, LocalDateTime createdRegister) {
	        this.categoryId = categoryId;
	        this.seller = seller;
	        this.name = name;
	        this.description = description;
	        this.price = price;
	        this.expiryDate = expiryDate;
	        this.createdRegister = createdRegister;
	    }

	 	}
