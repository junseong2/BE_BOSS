package com.onshop.shop.domain.crawl.service;

import com.onshop.shop.domain.crawl.dto.CrawledProductDTO;

/**
 * 알리익스프레스 상품 크롤링을 위한 서비스 인터페이스입니다.
 */
public interface AliCrawlerService {

    /**
     * 주어진 URL에서 상품 정보를 크롤링합니다.
     *
     * @param url 알리익스프레스 상품 상세 페이지 URL
     * @return 크롤링된 상품 정보 DTO
     */
    CrawledProductDTO crawl(String url);
}
