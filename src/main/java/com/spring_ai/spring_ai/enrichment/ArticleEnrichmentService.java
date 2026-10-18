package com.spring_ai.spring_ai.enrichment;

import com.spring_ai.spring_ai.article.Article;
import com.spring_ai.spring_ai.article.ArticleRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

@Service
public class ArticleEnrichmentService {

    private static final Duration TASK_TIMEOUT = Duration.ofMinutes(3);
    private static final String MODEL_NAME = "qwen3:8b";
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "MODELS_RELEASES",
            "AGENTS_MCP",
            "DEVELOPER_TOOLS",
            "RESEARCH_TECHNIQUES",
            "SECURITY_RELIABILITY"
    );

    private final ChatClient chatClient;
    private final ArticleRepository articleRepository;
    private final ArticleEnrichmentRepository enrichmentRepository;
    private final EnrichmentPersister persister;
    private final Executor executor;

    public ArticleEnrichmentService(
            ChatClient.Builder chatClientBuilder,
            ArticleRepository articleRepository,
            ArticleEnrichmentRepository enrichmentRepository, EnrichmentPersister persister,  @Qualifier("enrichmentExecutor") Executor executor
    ) {
        this.chatClient = chatClientBuilder.build();
        this.articleRepository = articleRepository;
        this.enrichmentRepository = enrichmentRepository;
        this.persister = persister;
        this.executor = executor;
    }

    public int enrichPendingArticles(int batchSize) {
        if (batchSize < 1 || batchSize > 5) {
            throw new IllegalArgumentException(
                    "Batch size must be between 1 and 5"
            );
        }

        PageRequest pageRequest = PageRequest.of(0, batchSize);

        List<ArticleInput> pending = articleRepository
                .findByEnrichmentStatusOrderByRetrievedAtAsc(
                        Article.EnrichmentStatus.PENDING, PageRequest.of(0, batchSize))
                .stream()
                .map(a -> new ArticleInput(a.getId(), a.getTitle(),
                        a.getCanonicalUrl(), a.getContent()))
                .toList();

        List<CompletableFuture<Boolean>> futures = pending.stream()
                .map(input -> CompletableFuture
                        .supplyAsync(() -> processOne(input), executor)
                        .orTimeout(TASK_TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                        .exceptionally(ex -> {
                            persister.fail(input.id());
                            return false;
                        }))
                .toList();

        return (int) futures.stream()
                .map(CompletableFuture::join)
                .filter(Boolean::booleanValue)
                .count();
    }


    private boolean processOne(ArticleInput input) {
        if (!persister.claim(input.id())) {
            return false; // someone else took it
        }
        try {
            ArticleEnrichmentResult result = analyze(input); // slow, no transaction
            validate(result);
            persister.complete(input.id(), result, MODEL_NAME);
            return true;
        } catch (Exception e) {
            persister.fail(input.id());
            return false;
        }
    }

    public void enrichArticle(Long id) {
        Article a = articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Article not found: " + id));
        if (a.getEnrichmentStatus() == Article.EnrichmentStatus.COMPLETED) {
            throw new IllegalStateException("Article is already enriched: " + id);
        }
        // reuse the same pipeline, but the claim must accept PENDING (or FAILED if you allow retry)
        if (!processOne(new ArticleInput(a.getId(), a.getTitle(),
                a.getCanonicalUrl(), a.getContent()))) {
            throw new IllegalStateException("Failed to enrich article " + id);
        }
    }



    private ArticleEnrichmentResult analyze(ArticleInput input) {
        String content = input.content() == null ? "" : input.content();

        if (content.length() > 6000) {
            content = content.substring(0, 6000);
        }

        String prompt = """
            ... (your prompt text unchanged) ...

            Article title:
            %s

            Article source URL:
            %s

            Article content:
            %s
            """.formatted(
                input.title(),
                input.url(),
                content
        );

        return chatClient.prompt()
                .user(prompt)
                .call()
                .entity(ArticleEnrichmentResult.class);
    }

    private void validate(ArticleEnrichmentResult result) {
        if (result == null || isBlank(result.summary())) {
            throw new IllegalArgumentException(
                    "AI returned an empty summary"
            );
        }

        if (result.primaryCategory() == null) {
            throw new IllegalArgumentException(
                    "AI returned no category"
            );
        }

        String category = result.primaryCategory()
                .trim()
                .toUpperCase(Locale.ROOT);

        if (!ALLOWED_CATEGORIES.contains(category)) {
            throw new IllegalArgumentException(
                    "Invalid category: " + result.primaryCategory()
            );
        }

        if (result.importanceScore() < 1
                || result.importanceScore() > 5) {
            throw new IllegalArgumentException(
                    "Importance score must be between 1 and 5"
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }



}
