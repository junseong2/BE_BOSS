package com.onshop.shop.store;

import com.onshop.shop.store.Store;
import com.onshop.shop.store.StoreService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class StoreController {

    @Autowired
    private StoreService storeService;

    @GetMapping("api/stores")
    public List<Store> getStoresNearby(
            @RequestParam double lat,
            @RequestParam double lng) {
        return storeService.getStoresNearby(lat, lng);
    }
}
