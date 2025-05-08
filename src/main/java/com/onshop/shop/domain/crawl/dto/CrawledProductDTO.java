package com.onshop.shop.domain.crawl.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class CrawledProductDTO {
    private String title;
    private String description;
    private int price;
    private List<String> images;
    private String uuid;
}