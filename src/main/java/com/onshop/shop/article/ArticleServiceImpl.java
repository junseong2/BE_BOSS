package com.onshop.shop.article;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;
import com.onshop.shop.user.User;

@Service
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;

    public ArticleServiceImpl(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    // ✅ 모든 게시물 조회
    @Override
    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(ArticleDTO::fromEntity) // Entity → DTO 변환
                .collect(Collectors.toList());
    }

    // ✅ 특정 게시물 조회
    @Override
    public ArticleDTO getArticleById(int id) {
        return articleRepository.findById(id)
                .map(ArticleDTO::fromEntity) // Entity → DTO 변환
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));
    }

    // ✅ 게시물 생성
    @Override
    public ArticleDTO createArticle(ArticleDTO articleDTO) {
        ArticleEntity articleEntity = new ArticleEntity();
        articleEntity.setArticleName(articleDTO.getArticleName());
        articleEntity.setArticle(articleDTO.getArticle());
        // UserEntity 설정 (userId가 있을 경우만)
        if (articleDTO.getUserId() > 0) {
            User user = new User();
            user.setUserId(articleDTO.getUserId());
            articleEntity.setUserEntity(user);
        }
        articleEntity.setWrittenDate(articleDTO.getWrittenDate());

        ArticleEntity savedArticle = articleRepository.save(articleEntity);
        return ArticleDTO.fromEntity(savedArticle); // 저장 후 DTO 반환
    }

    // ✅ 게시물 수정 (부분 업데이트 지원)
    @Override
    public ArticleDTO updateArticle(int id, ArticleDTO articleDTO) {
        ArticleEntity articleEntity = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));

        // 변경할 필드만 업데이트
        if (articleDTO.getArticleName() != null) {
            articleEntity.setArticleName(articleDTO.getArticleName());
        }
        if (articleDTO.getArticle() != null) {
            articleEntity.setArticle(articleDTO.getArticle());
        }

        ArticleEntity updatedArticle = articleRepository.save(articleEntity);
        return ArticleDTO.fromEntity(updatedArticle); // 업데이트 후 DTO 반환
    }

    // ✅ 게시물 삭제
    @Override
    public void deleteArticle(int id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("삭제할 게시물을 찾을 수 없습니다.");
        }
        articleRepository.deleteById(id);
    }
}
