package com.onshop.shop.domain.reply.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onshop.shop.domain.reply.entity.Reply;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    
    // 특정 게시물의 모든 댓글 조회
    List<Reply> findByArticle_ArticleId(Long articleId);
}