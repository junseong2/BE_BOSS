package com.onshop.shop.store;

import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.Comparator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StoreService {

    @Autowired
    private StoreRepository storeRepository; // StoreRepository 주입

    // 두 지점 간의 거리를 계산하는 메서드 (단위: km)
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // 지구 반지름 (단위: km)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // 거리 계산

        return distance;
    }

    public List<Store> getStoresNearby(double lat, double lng) {
        // 데이터베이스에서 모든 store 데이터를 가져옴
        List<Store> allStores = storeRepository.findAll();
        
        List<Store> nearbyStores = new ArrayList<>();

        // 주어진 위치(lat, lng)로부터 가까운 지점만 필터링
        for (Store store : allStores) {
            double distance = calculateDistance(lat, lng, store.getLatitude(), store.getLongitude());
            store.setDistance(distance); // Store 객체에 거리 추가

            // 3km 이내의 지점만 반환 (이 값은 조정 가능)
            if (distance <= 2) {
                nearbyStores.add(store);
            }
        }

        // 거리순으로 정렬
        Collections.sort(nearbyStores, Comparator.comparingDouble(Store::getDistance));

        return nearbyStores;
    }
}
