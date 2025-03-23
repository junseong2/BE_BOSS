package com.onshop.shop.business;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/business")
@RequiredArgsConstructor
public class BusinessController {

    private final BusinessService businessService;

    @PostMapping("/check")
    public ResponseEntity<String> checkBusiness(@RequestBody Map<String, String> request) {
        String businessNumber = request.get("businessNumber");
        String result = businessService.checkBusinessStatus(businessNumber);
        return ResponseEntity.ok(result);
    }
}
