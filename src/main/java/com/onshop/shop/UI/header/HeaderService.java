package com.onshop.shop.UI.header;

import java.util.List;

import org.springframework.stereotype.Service;

import com.onshop.shop.store.Seller;
import com.onshop.shop.store.SellerRepository;

@Service
public class HeaderService {
    private final HeaderRepository headerRepository;
    private final SellerRepository sellerRepository;

    public HeaderService(HeaderRepository headerRepository, SellerRepository sellerRepository) {
        this.headerRepository = headerRepository;
        this.sellerRepository = sellerRepository;
    }

    // 헤더 저장 (Seller 정보 포함)
    public Header saveHeader(Long sellerId, String name, String backgroundColor) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("해당 Seller가 존재하지 않습니다: " + sellerId));

        Header header = new Header();
        header.setSeller(seller);
        header.setName(name);
        header.setBackgroundColor(backgroundColor);

        return headerRepository.save(header);
    }

    // 모든 헤더 조회
    public List<Header> getAllHeaders() {
        return headerRepository.findAll();
    }

    
    public Header updateHeaderColorForSeller(Long sellerId, Long headerId, String backgroundColor) {
        Header header = headerRepository.findByHeaderIdAndSeller_SellerId(headerId, sellerId)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 헤더가 없거나 셀러가 일치하지 않습니다."));

        header.setBackgroundColor(backgroundColor);
        return headerRepository.save(header);
    }
    
    // 특정 헤더 배경 색상 업데이트
    public Header updateHeader(Long id, String backgroundColor) {
        Header header = headerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("해당 헤더가 존재하지 않습니다: " + id));

        header.setBackgroundColor(backgroundColor);
        return headerRepository.save(header);
    }

    public Header updateHeaderColor(Long id, String backgroundColor) {
        Header header = headerRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("해당 헤더가 존재하지 않습니다: " + id));

        header.setBackgroundColor(backgroundColor);
        return headerRepository.save(header);
    }
    // 특정 Seller의 헤더 조회
    public List<Header> getHeadersBySeller(Long sellerId) {
        Seller seller = sellerRepository.findById(sellerId)
            .orElseThrow(() -> new RuntimeException("해당 Seller가 존재하지 않습니다: " + sellerId));

        return headerRepository.findBySeller(seller);
    }
}
