package com.onshop.shop.store;
import org.springframework.stereotype.Service;
import java.util.Optional;

@Service
public class SellerService {

    private final SellerRepository sellerRepository;

    public SellerService(SellerRepository sellerRepository) {
        this.sellerRepository = sellerRepository;
    }

    public Optional<Seller> getSellerByStorename(String storeName) {
        Optional<Seller> seller = sellerRepository.findByStorename(storeName);

        if (seller.isEmpty()) {
            throw new RuntimeException("판매자를 찾을 수 없습니다: " + storeName);
        }

        return seller;
    }
}
