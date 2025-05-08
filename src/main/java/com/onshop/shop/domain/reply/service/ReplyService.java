package com.onshop.shop.domain.reply.service;

import java.util.List;

import com.onshop.shop.domain.reply.dto.ReplyDTO;

public interface ReplyService {
    // ✅ 특정 게시물의 댓글 조회
    List<ReplyDTO> getRepliesByArticleId(Long articleId);

    // ✅ 댓글 생성
    ReplyDTO createReply(ReplyDTO replyDTO);

    // ✅ 댓글 삭제
    void deleteReply(Long replyId);
}
