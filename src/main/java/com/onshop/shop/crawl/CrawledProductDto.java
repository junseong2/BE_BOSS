package com.onshop.shop.crawl;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;


@Data
@AllArgsConstructor
public class CrawledProductDto {
    private String title;
    private String description;
    private int price;
    private List<String> images;
    private String uuid;
}