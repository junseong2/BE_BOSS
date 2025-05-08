package com.onshop.shop.domain.article.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.onshop.shop.domain.article.entity.Article;

public interface ArticleRepository extends JpaRepository<Article, Long> {
}