package com.onshop.shop.reply;

import com.onshop.shop.user.User;
import com.onshop.shop.article.Article;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "reply")
public class Reply {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long replyId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "article_id", nullable = false, foreignKey = @ForeignKey(name = "fk_reply_article"))
    @OnDelete(action = OnDeleteAction.CASCADE) // 게시글 삭제 시 Reply 삭제
    private Article article;

    // ✅ 사용자와의 관계 (Many-to-One) → 사용자 삭제 시 Reply는 남아있고 user_id는 NULL 처리됨
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_reply_user"))
    @OnDelete(action = OnDeleteAction.SET_NULL) // 사용자 삭제 시 user_id를 NULL로 변경
    private User user;

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