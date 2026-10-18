package com.spring_ai.spring_ai.enrichment;

import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/enrichment")
public class ArticleEnrichmentController {

    private final ArticleEnrichmentService enrichmentService;

    public ArticleEnrichmentController(
            ArticleEnrichmentService enrichmentService) {
        this.enrichmentService = enrichmentService;
    }

    @PostMapping("/run")
    public Map<String, Object> enrich(
            @RequestParam(defaultValue = "5") int batchSize) {

        int completed =
                enrichmentService.enrichPendingArticles(batchSize);

        return Map.of(
                "status", "completed",
                "requestedBatchSize", batchSize,
                "enrichedArticles", completed
        );
    }

    @PostMapping("/{articleId}")
    public Map<String, Object> enrichArticle(
            @PathVariable Long articleId) {

        enrichmentService.enrichArticle(articleId);

        return Map.of(
                "status", "completed",
                "articleId", articleId
        );
    }



}
