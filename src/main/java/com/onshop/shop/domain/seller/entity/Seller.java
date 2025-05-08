package com.onshop.shop.domain.seller.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
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
@NoArgsConstructor
@Builder
public class Seller {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "seller_id")
    private Long sellerId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId;

    @Column(name = "storename", nullable = false, unique = true)
    private String storename;

    @Column(name = "description")
    private String description;

    @Lob
    @Column(name = "settings", columnDefinition = "TEXT")
    private String settings;

    @Lob
    @Column(name = "mobilesettings", columnDefinition = "TEXT")
    private String mobilesettings;

    @Column(name = "representative_name")
    private String representativeName;

    @Column(name = "business_registration_number")
    private String businessRegistrationNumber;

    @Column(name = "online_sales_number")
    private String onlineSalesNumber;

    @Column(name = "registration_status", nullable = false)
    private String registrationStatus;

    @Column(name = "application_date")
    private LocalDateTime applicationDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = this.createdAt == null ? LocalDateTime.now() : this.createdAt;
    }
}
