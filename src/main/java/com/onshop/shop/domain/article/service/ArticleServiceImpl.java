package com.onshop.shop.domain.article.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.onshop.shop.domain.article.dto.ArticleDTO;
import com.onshop.shop.domain.article.entity.Article;
import com.onshop.shop.domain.article.repository.ArticleRepository;
import com.onshop.shop.domain.user.entity.User;
import com.onshop.shop.global.exception.ResourceNotFoundException;

import lombok.RequiredArgsConstructor;

/**
 * {@code ArticleServiceImpl}는 FAQ 게시판의 게시글(질문)을 생성, 조회, 수정, 삭제하는 기능을 제공하는 서비스 구현체입니다.
 * <p>
 * 게시글은 작성자(User)와 연결될 수 있으며, 게시글 내용과 제목, 작성일 등을 포함합니다.
 * </p>
 * 
 * <p><b>주요 기능:</b></p>
 * <ul>
 *     <li>게시글 전체 조회</li>
 *     <li>게시글 상세 조회</li>
 *     <li>게시글 작성</li>
 *     <li>게시글 수정 (부분 업데이트 가능)</li>
 *     <li>게시글 삭제</li>
 * </ul>
 * 
 * @author 사용자
 */
@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;

    /**
     * 모든 게시글을 조회합니다.
     *
     * @return 게시글 DTO 리스트
     */
    @Override
    public List<ArticleDTO> getAllArticles() {
        return articleRepository.findAll().stream()
                .map(ArticleDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * 특정 ID를 가진 게시글을 조회합니다.
     *
     * @param id 조회할 게시글 ID
     * @return 해당 게시글 DTO
     * @throws RuntimeException 게시글이 존재하지 않을 경우
     */
    @Override
    public ArticleDTO getArticleById(Long id) {
        return articleRepository.findById(id)
                .map(ArticleDTO::fromEntity)
                .orElseThrow(() -> new ResourceNotFoundException("게시물을 찾을 수 없습니다."));
    }

    /**
     * 새로운 게시글을 생성합니다.
     * <p>
     * 작성자 ID가 0보다 작거나 같으면 작성자는 설정되지 않습니다.
     *
     * @param articleDTO 생성할 게시글 정보
     * @return 저장된 게시글 DTO
     */
    @Override
    public ArticleDTO createArticle(ArticleDTO articleDTO) {
        Article articleEntity = new Article();
        articleEntity.setArticleName(articleDTO.getArticleName());
        articleEntity.setArticle(articleDTO.getArticle());

        // 작성자 설정 (선택적)
        if (articleDTO.getUserId() > 0) {
            User user = new User();
            user.setUserId(articleDTO.getUserId());
            articleEntity.setUser(user);
        }

        articleEntity.setWrittenDate(articleDTO.getWrittenDate());

        Article savedArticle = articleRepository.save(articleEntity);
        return ArticleDTO.fromEntity(savedArticle);
    }

    /**
     * 기존 게시글을 수정합니다.
     * <p>
     * 제목 또는 내용이 null이 아닌 경우에만 해당 항목이 수정됩니다.
     *
     * @param id 수정할 게시글 ID
     * @param articleDTO 수정할 내용이 포함된 DTO
     * @return 수정된 게시글 DTO
     * @throws ResourceNotFoundException 게시글이 존재하지 않을 경우
     */
    @Override
    public ArticleDTO updateArticle(Long id, ArticleDTO articleDTO) {
        Article articleEntity = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("게시물을 찾을 수 없습니다."));

        if (articleDTO.getArticleName() != null) {
            articleEntity.setArticleName(articleDTO.getArticleName());
        }
        if (articleDTO.getArticle() != null) {
            articleEntity.setArticle(articleDTO.getArticle());
        }

        Article updatedArticle = articleRepository.save(articleEntity);
        return ArticleDTO.fromEntity(updatedArticle);
    }

    /**
     * 특정 ID를 가진 게시글을 삭제합니다.
     *
     * @param id 삭제할 게시글 ID
     * @throws ResourceNotFoundException 게시글이 존재하지 않을 경우
     */
    @Override
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("삭제할 게시물을 찾을 수 없습니다.");
        }
        articleRepository.deleteById(id);
    }
}
