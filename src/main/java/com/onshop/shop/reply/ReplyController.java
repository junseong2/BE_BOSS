package com.onshop.shop.reply;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/articles/{articleId}/comments")  // ✅ RESTful API 구조 반영
public class ReplyController {

    private final ReplyService replyService;

    public ReplyController(ReplyService replyService) {
        this.replyService = replyService;
    }

    // ✅ 특정 게시물의 댓글 조회
    @GetMapping
    public ResponseEntity<List<ReplyDTO>> getRepliesByArticleId(@PathVariable int articleId) {
        List<ReplyDTO> replies = replyService.getRepliesByArticleId(articleId);
        return ResponseEntity.ok(replies);
    }

    // ✅ 댓글 작성
    @PostMapping
    public ResponseEntity<ReplyDTO> createReply(@PathVariable int articleId, @RequestBody ReplyDTO replyDTO) {
        replyDTO.setArticleId(articleId); // 게시물 ID 설정
        ReplyDTO createdReply = replyService.createReply(replyDTO);
        return ResponseEntity.ok(createdReply);
    }

    // ✅ 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteReply(@PathVariable int articleId, @PathVariable int commentId) {
        replyService.deleteReply(commentId);
        return ResponseEntity.noContent().build();
    }
}
