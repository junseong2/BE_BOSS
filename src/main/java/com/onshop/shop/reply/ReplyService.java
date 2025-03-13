package com.onshop.shop.reply;

import java.util.List;

public interface ReplyService {
    // ✅ 특정 게시물의 댓글 조회
    List<ReplyDTO> getRepliesByArticleId(int articleId);

    // ✅ 댓글 생성
    ReplyDTO createReply(ReplyDTO replyDTO);

    // ✅ 댓글 삭제
    void deleteReply(int replyId);
}
