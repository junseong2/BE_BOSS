package com.onshop.shop.reply;

import com.onshop.shop.user.User;
import com.onshop.shop.article.ArticleEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "reply")
public class ReplyEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int replyId;

    // ✅ 게시물과의 관계 (Many-to-One)
    @ManyToOne
    @JoinColumn(name = "article_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reply_article"))
    private ArticleEntity article;

    // ✅ 사용자와의 관계 (Many-to-One, NULL 허용)
    @ManyToOne
    @JoinColumn(name = "writer_id", foreignKey = @ForeignKey(name = "fk_reply_user"))
    private User userEntity;

    // ✅ 댓글 본문
    @Column(nullable = false, columnDefinition = "TEXT")
    private String replyArticle;

    // ✅ 작성 날짜 (자동 설정)
    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime writtenDate;

    @PrePersist
    protected void onCreate() {
        this.writtenDate = LocalDateTime.now();
    }
}