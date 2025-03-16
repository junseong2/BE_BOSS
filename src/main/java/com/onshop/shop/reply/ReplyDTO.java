package com.onshop.shop.reply;

import java.time.LocalDateTime;

import com.onshop.shop.article.ArticleEntity;
import com.onshop.shop.user.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplyDTO {
    private int replyId;
    private int articleId;
    private int userId; // `UserEntity` 대신 `userId`만 저장
    private String replyArticle;
    private LocalDateTime writtenDate;

    // 생성자
    public ReplyDTO(int replyId, int articleId, int userId, String replyArticle, LocalDateTime writtenDate) {
        this.replyId = replyId;
        this.articleId = articleId;
        this.userId = userId;
        this.replyArticle = replyArticle;
        this.writtenDate = writtenDate;
    }

    // ✅ Entity → DTO 변환 메서드
    public static ReplyDTO fromEntity(ReplyEntity replyEntity) {
        return new ReplyDTO(
                replyEntity.getReplyId(),
                replyEntity.getArticle().getArticleId(),
                replyEntity.getUserEntity() != null ? replyEntity.getUserEntity().getUserId() : 0, // `UserEntity`가 NULL일 수도 있음
                replyEntity.getReplyArticle(),
                replyEntity.getWrittenDate()
        );
    }

    // ✅ DTO → Entity 변환 메서드 (댓글 생성 시 사용)
    public ReplyEntity toEntity(ArticleEntity articleEntity, User userEntity) {
        ReplyEntity replyEntity = new ReplyEntity();
        replyEntity.setArticle(articleEntity);
        replyEntity.setUserEntity(userEntity);
        replyEntity.setReplyArticle(this.replyArticle);
        replyEntity.setWrittenDate(LocalDateTime.now()); // 현재 시간 저장
        return replyEntity;
    }
}
