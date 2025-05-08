package com.onshop.shop.domain.product.service;

import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.onshop.shop.domain.product.entity.Product;
import com.onshop.shop.domain.product.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductSalesResetService {

    private final ProductRepository productRepository;

    // 매일 자정 - 일간 초기화
    @Scheduled(cron = "0 0 0 * * *")
    public void resetDailySales() {
        List<Product> products = productRepository.findAll();
        products.forEach(p -> p.setDailySales(0L)); // long 타입으로 초기화
        productRepository.saveAll(products);
    }

    // 매주 월요일 자정 - 주간 초기화
    @Scheduled(cron = "0 0 0 * * MON")
    public void resetWeeklySales() {
        List<Product> products = productRepository.findAll();
        products.forEach(p -> p.setDailySales(0L)); // long 타입으로 초기화
        productRepository.saveAll(products);
    }

    // 매월 1일 자정 - 월간 초기화
    @Scheduled(cron = "0 0 0 1 * *")
    public void resetMonthlySales() {
        List<Product> products = productRepository.findAll();
        products.forEach(p -> p.setDailySales(0L)); // long 타입으로 초기화
        productRepository.saveAll(products);
    }
}
