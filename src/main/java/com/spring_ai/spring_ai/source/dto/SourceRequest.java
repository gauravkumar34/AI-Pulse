package com.spring_ai.spring_ai.source.dto;

import com.spring_ai.spring_ai.source.SourceType;

public record SourceRequest(String name, SourceType sourceType, String feedUrl, Boolean active) {
}
