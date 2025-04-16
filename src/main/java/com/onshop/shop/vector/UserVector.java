package com.onshop.shop.vector;

import jakarta.persistence.*;
import lombok.*;

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
