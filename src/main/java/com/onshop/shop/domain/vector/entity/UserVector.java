package com.onshop.shop.domain.vector.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "user_vector")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserVector {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "update_count")
    private Long updateCount;

    @Transient
    private float[] avgVector;

    @Transient
    private float[] recentVector;

    @Column(name = "product_ids")
    private Long[] productIds;
}
