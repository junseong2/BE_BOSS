package com.onshop.shop.domain.reply.dto;

import java.time.LocalDateTime;

import com.onshop.shop.domain.article.entity.Article;
import com.onshop.shop.domain.reply.entity.Reply;
import com.onshop.shop.domain.user.entity.User;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReplyDTO {
    private Long replyId;
    private Long articleId;
    private Long userId; // `UserEntity` 대신 `userId`만 저장
    private String replyArticle;
    private LocalDateTime writtenDate;

    // 생성자
    public ReplyDTO(Long replyId, Long articleId, Long userId, String replyArticle, LocalDateTime writtenDate) {
        this.replyId = replyId;
        this.articleId = articleId;
        this.userId = userId;
        this.replyArticle = replyArticle;
        this.writtenDate = writtenDate;
    }

    // ✅ Entity → DTO 변환 메서드
    public static ReplyDTO fromEntity(Reply replyEntity) {
        return new ReplyDTO(
                replyEntity.getReplyId(),
                replyEntity.getArticle().getArticleId(),
                replyEntity.getUser() != null ? replyEntity.getUser().getUserId() : 0, // `UserEntity`가 NULL일 수도 있음
                replyEntity.getReplyArticle(),
                replyEntity.getWrittenDate()
        );
    }

    // ✅ DTO → Entity 변환 메서드 (댓글 생성 시 사용)
    public Reply toEntity(Article articleEntity, User userEntity) {
        Reply replyEntity = new Reply();
        replyEntity.setArticle(articleEntity);
        replyEntity.setUser(userEntity);
        replyEntity.setReplyArticle(this.replyArticle);
        replyEntity.setWrittenDate(LocalDateTime.now()); // 현재 시간 저장
        return replyEntity;
    }
}
