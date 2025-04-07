package com.onshop.shop.review;

import java.time.LocalDateTime;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SellerReviewAnswerDTO {
    private Long answerId;
    private Long reviewId;  // 리뷰 ID
    private String answerText;  // 응답 내용
    private String storename;  // 판매자 ID
    private LocalDateTime createdAt;  // 생성일시
    private LocalDateTime lastModifiedAt;  // 수정일시
}