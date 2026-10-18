package com.spring_ai.spring_ai.source.dto;
import com.spring_ai.spring_ai.source.SourceType;

import java.time.OffsetDateTime;

public record SourceResponse(
        Long id,
        String name,
        SourceType sourceType,
        String feedUrl,
        boolean active,
        OffsetDateTime lastFetchedAt,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
}

