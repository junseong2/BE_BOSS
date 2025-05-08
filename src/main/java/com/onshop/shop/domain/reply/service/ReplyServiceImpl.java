package com.onshop.shop.domain.reply.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.onshop.shop.domain.article.entity.Article;
import com.onshop.shop.domain.article.repository.ArticleRepository;
import com.onshop.shop.domain.reply.dto.ReplyDTO;
import com.onshop.shop.domain.reply.entity.Reply;
import com.onshop.shop.domain.reply.repository.ReplyRepository;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.domain.user.repository.UserRepository;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * {@code ReplyServiceImpl}는 FAQ 게시글에 대한 댓글(답글)의 생성, 조회, 삭제 기능을 제공하는 서비스 구현체입니다.
 * <p>
 * 이 클래스는 FAQ 댓글(답글) 도메인 로직을 처리하며, 게시글 및 사용자 정보와의 연계를 담당합니다.
 * 댓글은 게시글(article)에 종속됩니다.
 * 
 * <p><b>지원 기능:</b></p>
 * <ul>
 *     <li>특정 게시글의 댓글 목록 조회</li>
 *     <li>댓글 생성 (작성자 선택 가능)</li>
 *     <li>댓글 삭제</li>
 * </ul>
 * 
 */
@Service
@RequiredArgsConstructor
public class ReplyServiceImpl implements ReplyService {
    private final ReplyRepository replyRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    /**
     * 특정 게시글에 달린 모든 댓글을 조회합니다.
     *
     * @param articleId 댓글을 조회할 게시글 ID
     * @return 댓글 DTO 리스트
     */
    @Override
    public List<ReplyDTO> getRepliesByArticleId(Long articleId) {
        return replyRepository.findByArticle_ArticleId(articleId).stream()
                .map(ReplyDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 새로운 댓글을 작성합니다.
     * <p>
     * 댓글 작성 시 게시글이 존재해야 하며, 작성자는 선택적으로 입력 가능합니다.
     * 작성자 ID가 0 이하일 경우 익명 댓글로 간주됩니다.
     *
     * @param replyDTO 작성할 댓글 정보
     * @return 저장된 댓글 DTO
     * @throws ResourceNotFoundException 게시글이 존재하지 않을 경우
     */
    @Override
    public ReplyDTO createReply(ReplyDTO replyDTO) {
        // 게시물 찾기
        Article articleEntity = articleRepository.findById(replyDTO.getArticleId())
                .orElseThrow(() -> new ResourceNotFoundException("해당 게시물을 찾을 수 없습니다."));

        // 작성자 찾기 (익명 댓글 허용)
        User userEntity = null;
        if (replyDTO.getUserId() > 0) {
            userEntity = userRepository.findById(replyDTO.getUserId()).orElse(null);
        }

        // DTO → Entity 변환 후 저장
        Reply replyEntity = replyDTO.toEntity(articleEntity, userEntity);
        Reply savedReply = replyRepository.save(replyEntity);

        return ReplyDTO.fromEntity(savedReply);
    }

    /**
     * 댓글을 삭제합니다.
     *
     * @param replyId 삭제할 댓글 ID
     * @throws ResourceNotFoundException 댓글이 존재하지 않을 경우
     */
    @Override
    public void deleteReply(Long replyId) {
        Optional<Reply> reply = replyRepository.findById(replyId);
        if (reply.isEmpty()) {
            throw new ResourceNotFoundException("삭제할 댓글을 찾을 수 없습니다.");
        }
        replyRepository.deleteById(replyId);
    }
}
