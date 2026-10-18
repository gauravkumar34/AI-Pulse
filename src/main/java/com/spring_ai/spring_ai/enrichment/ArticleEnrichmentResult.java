package com.spring_ai.spring_ai.enrichment;

public record ArticleEnrichmentResult(
        String summary,
        String background,
        String whatChanged,
        String whyItMatters,
        String limitations,
        String developerImpact,
        String primaryCategory,
        int importanceScore,
        String importanceReason
) {}