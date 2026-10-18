package com.spring_ai.spring_ai.enrichment;

import com.spring_ai.spring_ai.article.Article;
import com.spring_ai.spring_ai.article.ArticleRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

@Component
public class EnrichmentPersister {

    private final ArticleRepository articleRepository;
    private final ArticleEnrichmentRepository enrichmentRepository;

    // constructor omitted
    EnrichmentPersister(ArticleRepository articleRepository, ArticleEnrichmentRepository enrichmentRepository){
        this.articleRepository = articleRepository;
        this.enrichmentRepository = enrichmentRepository;

    }

    @Transactional
    public boolean claim(Long id) {
        return articleRepository.transition(
                id, Article.EnrichmentStatus.PENDING,
                Article.EnrichmentStatus.PROCESSING) == 1;
    }

    @Transactional
    public void complete(Long id, ArticleEnrichmentResult result, String model) {
        Article article = articleRepository.findById(id).orElseThrow();
        enrichmentRepository.clearCurrent(id);
        int version = enrichmentRepository.countByArticleId(id) + 1;
        enrichmentRepository.save(new ArticleEnrichment(article, result, model, version));
        article.markEnrichmentCompleted();
    }

    @Transactional
    public void fail(Long id) {
        articleRepository.findById(id)
                .filter(a -> a.getEnrichmentStatus() == Article.EnrichmentStatus.PROCESSING)
                .ifPresent(Article::markEnrichmentFailed);
    }
}