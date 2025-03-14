package com.onshop.shop.store;

import org.springframework.stereotype.Service;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    public Store getStoreByStorename(String storename) {
        return storeRepository.findByStorename(storename)
                .orElseThrow(() -> new RuntimeException("Store not found"));
    }
}
