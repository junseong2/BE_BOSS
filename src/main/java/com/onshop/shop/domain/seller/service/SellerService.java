package com.onshop.shop.domain.seller.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.onshop.shop.domain.seller.dto.SellerStatsDTO;
import com.onshop.shop.domain.seller.dto.SellerStoresDTO;
import com.onshop.shop.domain.seller.entity.Seller;

public interface SellerService {

    Optional<Seller> getSellerByStorename(String storeName);
    Optional<Seller> getSellerByUserId(Long userId);
    Optional<Seller> getSellerById(Long sellerId);
    Seller updateSellerSettings(Long sellerId, String settings);
    Seller updateSellerMobilesettings(Long sellerId, String settings);
    Seller updateSellerAllSettings(Long sellerId, List<Map<String, Object>> settings);
    Seller updateSellerAllMobileSettings(Long sellerId, List<Map<String, Object>> mobileSettings);
    Seller updateHeaderBackgroundColor(Long sellerId, String backgroundColor);
    Seller findBysellerId(Long sellerId);
    Seller save(Seller seller);
    Seller registerSeller(Long userId, String storename, String description, String RepresentativeName,
                          String businessRegistrationNumber, String onlineSalesNumber);
    boolean isUserAlreadySeller(Long userId);
    void approveSeller(Long sellerId);
    void rejectSeller(Long sellerId);
    List<Seller> getAllSellers();
    boolean isUserSeller(Long userId);
    SellerStatsDTO getSellerStats();
    List<SellerStoresDTO> getAllStores(int page, int size);
}

