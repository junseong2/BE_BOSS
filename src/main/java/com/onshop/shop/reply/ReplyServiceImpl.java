package com.onshop.shop.reply;

import com.onshop.shop.article.Article;
import com.onshop.shop.article.ArticleRepository;
import com.onshop.shop.user.User;
import com.onshop.shop.user.UserRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReplyServiceImpl implements ReplyService {
    private final ReplyRepository replyRepository;
    private final ArticleRepository articleRepository;
    private final UserRepository userRepository;

    public ReplyServiceImpl(ReplyRepository replyRepository, ArticleRepository articleRepository, UserRepository userRepository) {
        this.replyRepository = replyRepository;
        this.articleRepository = articleRepository;
        this.userRepository = userRepository;
    }

    // ✅ 특정 게시물의 댓글 조회
    @Override
    public List<ReplyDTO> getRepliesByArticleId(Long articleId) {
        return replyRepository.findByArticle_ArticleId(articleId).stream()
                .map(ReplyDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // ✅ 댓글 생성
    @Override
    public ReplyDTO createReply(ReplyDTO replyDTO) {
        // 게시물 찾기
        Article articleEntity = articleRepository.findById(replyDTO.getArticleId())
                .orElseThrow(() -> new RuntimeException("해당 게시물을 찾을 수 없습니다."));

        // 작성자 찾기 (익명 댓글 허용)
        User userEntity = null;
        if (replyDTO.getUserId() > 0) {
            userEntity = userRepository.findById(replyDTO.getUserId()).orElse(null);
        }

        // DTO → Entity 변환 후 저장
        Reply replyEntity = replyDTO.toEntity(articleEntity, userEntity);
        Reply savedReply = replyRepository.save(replyEntity);

        return ReplyDTO.fromEntity(savedReply); // 저장 후 DTO 반환
    }

    // ✅ 댓글 삭제
    @Override
    public void deleteReply(Long replyId) {
        Optional<Reply> reply = replyRepository.findById(replyId);
        if (reply.isEmpty()) {
            throw new RuntimeException("삭제할 댓글을 찾을 수 없습니다.");
        }
        replyRepository.deleteById(replyId);
    }
}
