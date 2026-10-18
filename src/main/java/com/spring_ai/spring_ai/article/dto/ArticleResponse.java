package com.spring_ai.spring_ai.article.dto;

import java.time.OffsetDateTime;

public record ArticleResponse(
        Long id,
        String title,
        String canonicalUrl,
        String author,
        OffsetDateTime publishedAt,
        OffsetDateTime retrievedAt,
        String content,
        String enrichmentStatus,
        String sourceName,
        String summary,
        String primaryCategory,
        Integer importanceScore,
        String importanceReason
) {
}