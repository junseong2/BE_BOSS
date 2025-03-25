package com.onshop.shop.store;

import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // ✅ Lombok으로 기본 생성자 추가
@AllArgsConstructor
public class UIElementDTO {
    private String type;       // "header", "banner", "grid"
    private Map<String, Object> data;
    private int sortOrder;     // 순서 관리 (1, 2, 3...)
}