package com.onshop.shop.reply;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReplyRepository extends JpaRepository<Reply, Long> {
    
    // ✅ 특정 게시물의 모든 댓글 조회
    List<Reply> findByArticle_ArticleId(Long articleId);
}