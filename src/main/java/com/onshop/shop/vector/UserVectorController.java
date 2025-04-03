package com.onshop.shop.vector;

import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/vector")
@RequiredArgsConstructor
public class UserVectorController {

    private final UserVectorService userVectorService;

    /**
     * ✅ 테스트용 POST 요청
     * 예: POST /vector/recommend?userId=1&topK=20&neighborCount=3
     */
    @PostMapping("/update")
    public String updateUserVector(
            @RequestParam Long userId,
            @RequestParam Long productId
    ) {
        userVectorService.handleAddToCartAndUpdateUserVector(userId, productId);
        return "✅ 사용자 벡터 업데이트 완료: userId=" + userId + ", productId=" + productId;
    }
    
    // 예시 postman url : GET /vector/recommend?userId=1&topK=20&neighborCount=3
    /**
     * ✅ 예시 응답 (JSON):
     * [
     *   101,
     *   202,
     *   303,
     *   404,
     *   505,
     *   606,
     *   707,
     *   808,
     *   909,
     *   1001,
     *   1102,
     *   1203,
     *   1304,
     *   1405,
     *   1506,
     *   1607,
     *   1708,
     *   1809,
     *   1900,
     *   2001
     * ]
     */
    @GetMapping("/recommend")
    public List<Long> recommendProducts(
            @RequestParam Long userId,
            @RequestParam(name = "topK", defaultValue = "20") int topK,
            @RequestParam(name = "neighborCount", defaultValue = "3") int neighborCount
    ) {
        return userVectorService.recommendProducts(userId, topK, neighborCount);
    }
    
}