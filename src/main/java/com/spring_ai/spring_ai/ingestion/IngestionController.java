package com.spring_ai.spring_ai.ingestion;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/ingestion")
public class IngestionController {

    private final RssFeedCollector collector;

    public IngestionController(RssFeedCollector collector) {
        this.collector = collector;
    }

    @PostMapping("/run")
    public Map<String, Object> run() throws Exception {
        int saved = collector.collectAll();

        return Map.of(
                "status", "completed",
                "newArticlesSaved", saved
        );
    }
}