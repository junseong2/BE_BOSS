package com.onshop.shop.domain.reply.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.onshop.shop.domain.reply.dto.ReplyDTO;
import com.onshop.shop.domain.reply.service.ReplyService;

import lombok.RequiredArgsConstructor;

/**
 * {@code ReplyController}는 FAQ 게시판의 각 게시글에 달리는 댓글(답글)을 관리하는 REST 컨트롤러입니다.
 * <p>
 * 이 컨트롤러는 특정 게시글에 대한 댓글 목록 조회, 댓글 작성, 댓글 삭제 기능을 제공합니다.
 * 댓글은 {@code /articles/{articleId}/comments} 경로 하위에서 관리됩니다.
 * 
 * <p><b>기능 목록:</b></p>
 * <ul>
 *   <li>게시글별 댓글 조회</li>
 *   <li>댓글 작성</li>
 *   <li>댓글 삭제</li>
 * </ul>
 * 
 * @author 사용자
 */
@RestController
@RequestMapping("/articles/{articleId}/comments")
@RequiredArgsConstructor
public class ReplyController {

    private final ReplyService replyService;

    /**
     * 특정 FAQ 게시글에 달린 모든 댓글(답글)을 조회합니다.
     *
     * @param articleId 댓글을 조회할 게시글의 ID
     * @return 댓글 목록
     */
    @GetMapping
    public ResponseEntity<List<ReplyDTO>> getRepliesByArticleId(@PathVariable Long articleId) {
        List<ReplyDTO> replies = replyService.getRepliesByArticleId(articleId);
        return ResponseEntity.ok(replies);
    }

    /**
     * 특정 FAQ 게시글에 댓글(답글)을 작성합니다.
     *
     * @param articleId 댓글을 작성할 게시글의 ID
     * @param replyDTO 작성할 댓글의 내용
     * @return 생성된 댓글 객체
     */
    @PostMapping
    public ResponseEntity<ReplyDTO> createReply(@PathVariable Long articleId, @RequestBody ReplyDTO replyDTO) {
        replyDTO.setArticleId(articleId); // 게시물 ID 설정
        ReplyDTO createdReply = replyService.createReply(replyDTO);
        return ResponseEntity.ok(createdReply);
    }

    /**
     * 특정 FAQ 게시글에 달린 댓글(답글)을 삭제합니다.
     *
     * @param articleId 게시글 ID (댓글 소속 확인용)
     * @param commentId 삭제할 댓글의 ID
     * @return 삭제 성공 시 No Content 응답
     */
    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> deleteReply(@PathVariable Long articleId, @PathVariable Long commentId) {
        replyService.deleteReply(commentId);
        return ResponseEntity.noContent().build();
    }
}
