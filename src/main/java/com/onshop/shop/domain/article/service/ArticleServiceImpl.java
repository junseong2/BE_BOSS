package com.onshop.shop.domain.article.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.onshop.shop.domain.article.dto.ArticleDTO;
import com.onshop.shop.domain.article.entity.Article;
import com.onshop.shop.domain.article.repository.ArticleRepository;
import com.onshop.shop.domain.user.entity.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {
    private final ArticleRepository articleRepository;


    // 모든 게시물 조회
    @Override
    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(ArticleDTO::fromEntity) // Entity → DTO 변환
                .collect(Collectors.toList());
    }

    // 특정 게시물 조회
    @Override
    public ArticleDTO getArticleById(Long id) {
        return articleRepository.findById(id)
                .map(ArticleDTO::fromEntity) // Entity → DTO 변환
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));
    }

    // 게시물 생성
    @Override
    public ArticleDTO createArticle(ArticleDTO articleDTO) {
        Article articleEntity = new Article();
        articleEntity.setArticleName(articleDTO.getArticleName());
        articleEntity.setArticle(articleDTO.getArticle());
        // UserEntity 설정 (userId가 있을 경우만)
        if (articleDTO.getUserId() > 0) {
            User user = new User();
            user.setUserId(articleDTO.getUserId());
            articleEntity.setUser(user);
        }
        articleEntity.setWrittenDate(articleDTO.getWrittenDate());

        Article savedArticle = articleRepository.save(articleEntity);
        return ArticleDTO.fromEntity(savedArticle); // 저장 후 DTO 반환
    }

    // 게시물 수정 (부분 업데이트 지원)
    @Override
    public ArticleDTO updateArticle(Long id, ArticleDTO articleDTO) {
        Article articleEntity = articleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("게시물을 찾을 수 없습니다."));

        // 변경할 필드만 업데이트
        if (articleDTO.getArticleName() != null) {
            articleEntity.setArticleName(articleDTO.getArticleName());
        }
        if (articleDTO.getArticle() != null) {
            articleEntity.setArticle(articleDTO.getArticle());
        }

        Article updatedArticle = articleRepository.save(articleEntity);
        return ArticleDTO.fromEntity(updatedArticle); // 업데이트 후 DTO 반환
    }

    // 게시물 삭제
    @Override
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new RuntimeException("삭제할 게시물을 찾을 수 없습니다.");
        }
        articleRepository.deleteById(id);
    }
}
