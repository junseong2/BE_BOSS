package com.onshop.shop.article;

import java.time.LocalDateTime;

import com.onshop.shop.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "article")
public class ArticleEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int articleId;

    @Column(nullable = false, length = 255)
    private String articleName;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String article;

    // UserEntity와 연결, user_id가 NULL이 될 수도 있도록 설정
    @ManyToOne
    @JoinColumn(name = "user_id", foreignKey = @ForeignKey(name = "fk_article_user"), nullable = true)
    private User userEntity;

    @Column(columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime writtenDate;

    // 글 작성 시 자동으로 현재 시간 저장
    @PrePersist
    protected void onCreate() {
        this.writtenDate = LocalDateTime.now();
    }
}