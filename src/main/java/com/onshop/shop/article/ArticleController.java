package com.onshop.shop.article;

import com.onshop.shop.article.ArticleService;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("/articles")  // ✅ RESTful 경로 수정
@Slf4j
public class ArticleController {

    private final ArticleService articleService;

    public ArticleController(ArticleService articleService) {
        this.articleService = articleService;
    }

    // ✅ 모든 게시물 조회
    @GetMapping
    public ResponseEntity<List<ArticleDTO>> getAllArticles() {
    	
        List<ArticleDTO> articles = articleService.getAllArticles();
        log.info(articles.toString());
        return ResponseEntity.ok(articles);
        
    }

    // ✅ 특정 게시물 조회
    @GetMapping("/{id}")
    public ResponseEntity<ArticleDTO> getArticleById(@PathVariable Long id) {
        ArticleDTO article = articleService.getArticleById(id);
        return ResponseEntity.ok(article);
    }

    // ✅ 게시물 생성
    @PostMapping
    public ResponseEntity<ArticleDTO> createArticle(@RequestBody ArticleDTO articleDTO) {
    	System.out.println("받은 데이터: " + articleDTO); // ✅ 요청 데이터 확인용 로그 추가
    	ArticleDTO createdArticle = articleService.createArticle(articleDTO);
        return ResponseEntity.ok(createdArticle);
    }

    // ✅ 게시물 수정 (부분 업데이트 지원)
    @PatchMapping("/{id}")
    public ResponseEntity<ArticleDTO> updateArticle(@PathVariable Long id, @RequestBody ArticleDTO articleDTO) {
        ArticleDTO updatedArticle = articleService.updateArticle(id, articleDTO);
        return ResponseEntity.ok(updatedArticle);
    }

    // ✅ 게시물 삭제
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        log.info("del id:{}",id);
        return ResponseEntity.noContent().build();
    }
}