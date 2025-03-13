package com.onshop.shop.store;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StoreRepository extends JpaRepository<Store, Long> {
    // Store 엔티티에 대해 필요한 메서드 추가 (기본적인 CRUD는 JpaRepository가 제공)
}
