package com.onshop.shop.article;

import java.util.List;

public interface ArticleService {
    // ✅ 모든 게시물 조회
    List<ArticleDTO> getAllArticles();

    // ✅ 특정 게시물 조회
    ArticleDTO getArticleById(int id);

    // ✅ 게시물 생성
    ArticleDTO createArticle(ArticleDTO articleDTO);

    // ✅ 게시물 수정 (부분 업데이트 지원)
    ArticleDTO updateArticle(int id, ArticleDTO articleDTO);

    // ✅ 게시물 삭제
    void deleteArticle(int id);
}