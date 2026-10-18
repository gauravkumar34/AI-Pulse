package com.spring_ai.spring_ai.article;

import com.spring_ai.spring_ai.article.dto.ArticleDetailResponse;
import com.spring_ai.spring_ai.article.dto.ArticleResponse;
import com.spring_ai.spring_ai.enrichment.ArticleEnrichment;
import com.spring_ai.spring_ai.enrichment.ArticleEnrichmentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/articles")
public class ArticleController {

    private final ArticleRepository articleRepository;
    private final ArticleEnrichmentRepository enrichmentRepository;
    public ArticleController(ArticleRepository articleRepository, ArticleEnrichmentRepository enrichmentRepository) {
        this.articleRepository = articleRepository;
        this.enrichmentRepository =enrichmentRepository;
    }

    @GetMapping
    public List<ArticleResponse> getLatestArticles() {
        List<Article> articles =
                articleRepository.findTop50ByOrderByRetrievedAtDesc();

        List<Long> articleIds = articles.stream()
                .map(Article::getId)
                .toList();

        Map<Long, ArticleEnrichment> enrichmentsByArticleId =
                enrichmentRepository
                        .findByCurrentTrueAndArticleIdIn(articleIds)
                        .stream()
                        .collect(Collectors.toMap(
                                enrichment -> enrichment.getArticle().getId(),
                                enrichment -> enrichment
                        ));

        return articles.stream()
                .map(article -> {
                    ArticleEnrichment enrichment =
                            enrichmentsByArticleId.get(article.getId());

                    return new ArticleResponse(
                            article.getId(),
                            article.getTitle(),
                            article.getCanonicalUrl(),
                            article.getAuthor(),
                            article.getPublishedAt(),
                            article.getRetrievedAt(),
                            article.getContent(),
                            article.getEnrichmentStatus().name(),
                            article.getSource().getName(),
                            enrichment != null ? enrichment.getSummary() : null,
                            enrichment != null
                                    ? enrichment.getPrimaryCategory() : null,
                            enrichment != null && enrichment.getImportanceScore() != null
                                    ? enrichment.getImportanceScore().intValue() : null,
                            enrichment != null
                                    ? enrichment.getImportanceReason() : null
                    );
                })
                .toList();
    }

    @GetMapping("/{id}")
    public ArticleDetailResponse getArticleById(@PathVariable Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Article not found: " + id
                ));

        ArticleEnrichment enrichment = enrichmentRepository
                .findFirstByArticleIdAndCurrentTrue(id)
                .orElse(null);

        return new ArticleDetailResponse(
                article.getId(),
                article.getTitle(),
                article.getCanonicalUrl(),
                article.getAuthor(),
                article.getPublishedAt(),
                article.getRetrievedAt(),
                article.getContent(),
                article.getEnrichmentStatus().name(),
                article.getSource().getName(),
                enrichment == null ? null : enrichment.getSummary(),
                enrichment == null ? null : enrichment.getBackground(),
                enrichment == null ? null : enrichment.getWhatChanged(),
                enrichment == null ? null : enrichment.getWhyItMatters(),
                enrichment == null ? null : enrichment.getLimitations(),
                enrichment == null ? null : enrichment.getDeveloperImpact(),
                enrichment == null ? null : enrichment.getPrimaryCategory(),
                enrichment == null ? null : enrichment.getImportanceScore() == null
                                            ? null : enrichment.getImportanceScore().intValue(),
                enrichment == null ? null : enrichment.getImportanceReason()
        );
    }


    @GetMapping("/discover")
    public List<ArticleResponse> getDiscoverFeed(@RequestParam(required = false)  String category) {
        List<ArticleEnrichment> enrichments;

        if(category == null || category.isBlank()){
            enrichments =
                    enrichmentRepository
                            .findByCurrentTrueOrderByImportanceScoreDescCreatedAtDesc(
                                    PageRequest.of(0, 50)
                            );
        } else {
            enrichments =
                    enrichmentRepository
                        .findByCurrentTrueAndPrimaryCategoryOrderByImportanceScoreDescCreatedAtDesc(
                                category, PageRequest.of(0,50)
                        );
        }

        return enrichments.stream()
                .map(enrichment -> {
                    Article article = enrichment.getArticle();

                    return new ArticleResponse(
                            article.getId(),
                            article.getTitle(),
                            article.getCanonicalUrl(),
                            article.getAuthor(),
                            article.getPublishedAt(),
                            article.getRetrievedAt(),
                            article.getContent(),
                            article.getEnrichmentStatus().name(),
                            article.getSource().getName(),
                            enrichment.getSummary(),
                            enrichment.getPrimaryCategory(),
                            enrichment.getImportanceScore() != null
                                    ? enrichment.getImportanceScore().intValue()
                                    : null,
                            enrichment.getImportanceReason()
                    );
                })
                .toList();
    }


}