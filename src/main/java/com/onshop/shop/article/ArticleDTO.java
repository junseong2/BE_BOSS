package com.onshop.shop.article;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ArticleDTO {
    private int articleId;
    private String articleName;
    private String article;
    private int userId; // UserEntity 대신 userId만 포함
    private LocalDateTime writtenDate;

    public ArticleDTO(int articleId, String articleName, String article, int userId, LocalDateTime writtenDate) {
        this.articleId = articleId;
        this.articleName = articleName;
        this.article = article;
        this.userId = userId;
        this.writtenDate = writtenDate;
    }

    // Entity → DTO 변환 메서드
    public static ArticleDTO fromEntity(ArticleEntity articleEntity) {
        return new ArticleDTO(
                articleEntity.getArticleId(),
                articleEntity.getArticleName(),
                articleEntity.getArticle(),
                articleEntity.getUserEntity() != null ? articleEntity.getUserEntity().getUserId() : 0, // UserEntity가 NULL일 수도 있음
                articleEntity.getWrittenDate()
        );
    }
}
