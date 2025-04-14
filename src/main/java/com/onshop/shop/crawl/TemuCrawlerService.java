package com.onshop.shop.crawl;

import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class TemuCrawlerService {

    public CrawledProductDto crawl(String url) {
        // TODO: 추후 Temu용 크롤링 로직 구현 예정

        return new CrawledProductDto(
                "테무 상품명",
                "할인율: 30%\n할인가격: 9,900원\n\n테무 설명 텍스트입니다.",
                15900,
                Collections.singletonList("desc_0.jpg"),
                "dummy-temu-uuid"
        );
    }
}