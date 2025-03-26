package com.onshop.shop.UI.header;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/UI/headers")
public class HeaderController {
    private final HeaderService headerService;

    public HeaderController(HeaderService headerService) {
        this.headerService = headerService;
    }

    // 헤더 저장 API
    @PostMapping("/save")
    public ResponseEntity<Header> saveHeader(@RequestBody Map<String, String> request) {
        Long sellerId = Long.parseLong(request.get("sellerId")); // 문자열을 Long으로 변환
        String name = request.get("name");
        String backgroundColor = request.get("backgroundColor");

        Header savedHeader = headerService.saveHeader(sellerId, name, backgroundColor);
        return ResponseEntity.ok(savedHeader);
    }
    
    
    @PutMapping("/{id}/updateBackgroundColor")
    public ResponseEntity<Header> updateHeaderBackgroundColor(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String backgroundColor = request.get("backgroundColor");
        Header updatedHeader = headerService.updateHeaderColor(id, backgroundColor);
        return ResponseEntity.ok(updatedHeader);
    }
    @PutMapping("/seller/{sellerId}/headers/{id}/updateBackgroundColor")
    public ResponseEntity<Header> updateHeaderBackgroundColor(
            @PathVariable Long sellerId,
            @PathVariable Long id,
            @RequestBody Map<String, String> request) {
        
        String backgroundColor = request.get("backgroundColor");
        Header updatedHeader = headerService.updateHeaderColorForSeller(sellerId, id, backgroundColor);
        
        return ResponseEntity.ok(updatedHeader);
    }
    
    // 모든 헤더 조회 API
    @GetMapping("/all")
    public ResponseEntity<List<Header>> getAllHeaders() {
        return ResponseEntity.ok(headerService.getAllHeaders());
    }

    // 특정 헤더 배경 색상 변경 API
    @PutMapping("/{id}/update")
    public ResponseEntity<Header> updateHeader(@PathVariable Long id, @RequestBody Map<String, String> request) {
        String backgroundColor = request.get("backgroundColor");
        Header updatedHeader = headerService.updateHeader(id, backgroundColor);
        return ResponseEntity.ok(updatedHeader);
        
        
    }
    
    
    
    // 특정 Seller의 헤더 조회 API
    @GetMapping("/seller/{sellerId}")
    public ResponseEntity<List<Header>> getHeadersBySeller(@PathVariable Long sellerId) {
        return ResponseEntity.ok(headerService.getHeadersBySeller(sellerId));
    }
}
