
package com.spring_ai.spring_ai.article.dto;

import java.time.OffsetDateTime;

public record ArticleDetailResponse(
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
        String background,
        String whatChanged,
        String whyItMatters,
        String limitations,
        String developerImpact,
        String primaryCategory,
        Integer importanceScore,
        String importanceReason
) {}
