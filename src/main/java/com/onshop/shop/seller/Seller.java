package com.onshop.shop.seller;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.onshop.shop.category.Category;
import com.onshop.shop.product.Product;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Data
@Entity
@Getter
@Setter
@Table(name = "seller")
@AllArgsConstructor
@Builder
public class Seller {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "seller_id")
	private Long sellerId; // 판매자 고유 ID

	@Column(name = "user_id", nullable = false)
	private Long userId; // 사용자 ID (외래 키 참조)

	@Column(name = "storename", unique = true, nullable = false)
	private String storename; // 상점 이름

	@Column(name = "description")
	private String description; // 상점 설명

	@Lob
	@Column(name = "settings", columnDefinition = "LONGTEXT")
	private String settings; // 판매자 설정 (JSON 형식으로 저장)

	@Lob
	@Column(name = "mobilesettings", columnDefinition = "LONGTEXT")
	private String mobilesettings; // 판매자 설정 (JSON 형식으로 저장)

	@Column(name = "header_id", nullable = true)
	private Integer headerId; // 상단바 ID (디자인 변경 가능)

	@Column(name = "menu_bar_id", nullable = true)
	private Integer menuBarId; // 메뉴바 ID (메뉴 스타일 변경 가능)

	@Column(name = "navigation_id", nullable = true)
	private Integer navigationId; // 네비게이션 ID (페이지 이동 방식)

	@Column(name = "seller_menubar_color", length = 7, nullable = true)
	private String sellerMenubarColor; // 메뉴바 색상 (예: "#808080")

	@Column(name = "representative_name", nullable = true)
	private String representativeName;

	@Column(name = "business_registration_number", nullable = true)
	private String businessRegistrationNumber;

	@Column(name = "online_sales_number")
	private String onlineSalesNumber;

	@Column(name = "registration_status", nullable = false)
	private String registrationStatus; // 판매업 등록 상태 (예: '등록 대기', '등록 완료', '등록 거부')

	@Column(name = "application_date", columnDefinition = "DATETIME")
	private LocalDateTime applicationDate; // 판매업 등록 신청 날짜

	@Column(name = "created_at", columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
	private LocalDateTime createdAt; // 생성일

	public Seller() {
	}

	public Seller(Long sellerId, Long userId, String storename, String description, Integer headerId, Integer menuBarId,
			Integer navigationId, String sellerMenubarColor, LocalDateTime createdAt, String registrationStatus,
			LocalDateTime applicationDate, String representativeName, String businessRegistrationNumber,
			String onlineSalesNumber, String settings, String mobilesettings) {
		this.sellerId = sellerId;
		this.userId = userId;
		this.storename = storename;
		this.description = description;
		this.headerId = headerId;
		this.menuBarId = menuBarId;
		this.navigationId = navigationId;
		this.sellerMenubarColor = sellerMenubarColor;
		this.createdAt = createdAt;
		this.registrationStatus = registrationStatus;
		this.applicationDate = applicationDate;
		this.representativeName = representativeName;
		this.businessRegistrationNumber = businessRegistrationNumber;
		this.onlineSalesNumber = onlineSalesNumber;
		this.settings = settings;
		this.mobilesettings = mobilesettings;
	}

}
