package com.onshop.shop.reply;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ReplyRepository extends JpaRepository<ReplyEntity, Integer> {

    // ✅ 특정 게시물의 모든 댓글 조회
    List<ReplyEntity> findByArticle_ArticleId(int articleId);
}