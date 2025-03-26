package com.onshop.shop.article;

import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Getter
@Setter
public class ArticleDTO {
    private Long articleId;
    private String articleName;
    private String article;
    private Long userId; // UserEntity 대신 userId만 포함
    private LocalDateTime writtenDate;

    public ArticleDTO(Long articleId, String articleName, String article, Long userId, LocalDateTime writtenDate) {
        this.articleId = articleId;
        this.articleName = articleName;
        this.article = article;
        this.userId = userId;
        this.writtenDate = writtenDate;
    }

    // Entity → DTO 변환 메서드
    public static ArticleDTO fromEntity(Article articleEntity) {
        return new ArticleDTO(
                articleEntity.getArticleId(),
                articleEntity.getArticleName(),
                articleEntity.getArticle(),
                articleEntity.getUser() != null ? articleEntity.getUser().getUserId() : 0, // UserEntity가 NULL일 수도 있음
                articleEntity.getWrittenDate()
        );
    }
}
