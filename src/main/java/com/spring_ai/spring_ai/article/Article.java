package com.spring_ai.spring_ai.article;

import com.spring_ai.spring_ai.source.Source;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(
        name = "articles",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_articles_canonical_url",
                        columnNames = "canonical_url"
                )
        },
        indexes = {
                @Index(name = "idx_articles_retrieved_at", columnList = "retrieved_at"),
                @Index(name = "idx_articles_published_at", columnList = "published_at"),
                @Index(name = "idx_articles_source_id", columnList = "source_id")
        }
)
public class Article {

    public enum EnrichmentStatus {
        PENDING, PROCESSING, COMPLETED, FAILED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "source_id", nullable = false)
    private Source source;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "canonical_url", nullable = false, unique = true)
    private String canonicalUrl;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    private String author;

    @Column(name = "published_at")
    private OffsetDateTime publishedAt;

    @Column(name = "retrieved_at", nullable = false)
    private OffsetDateTime retrievedAt = OffsetDateTime.now();

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "content_hash", length = 64)
    private String contentHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "enrichment_status", nullable = false, length = 20)
    private EnrichmentStatus enrichmentStatus = EnrichmentStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Article() {
    }

    public Article(
            Source source,
            String externalId,
            String canonicalUrl,
            String title,
            String author,
            OffsetDateTime publishedAt,
            String content
    ) {
        this.source = source;
        this.externalId = externalId;
        this.canonicalUrl = canonicalUrl;
        this.title = title;
        this.author = author;
        this.publishedAt = publishedAt;
        this.content = content;
        this.retrievedAt = OffsetDateTime.now();
    }

    public Long getId() { return id; }
    public Source getSource() { return source; }
    public String getExternalId() { return externalId; }
    public String getCanonicalUrl() { return canonicalUrl; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public OffsetDateTime getPublishedAt() { return publishedAt; }
    public OffsetDateTime getRetrievedAt() { return retrievedAt; }
    public String getContent() { return content; }
    public String getContentHash() { return contentHash; }
    public EnrichmentStatus getEnrichmentStatus() { return enrichmentStatus; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }

    public void markEnrichmentProcessing() {
        this.enrichmentStatus = EnrichmentStatus.PROCESSING;
    }

    public void markEnrichmentCompleted() {
        this.enrichmentStatus = EnrichmentStatus.COMPLETED;
    }

    public void markEnrichmentFailed() {
        this.enrichmentStatus = EnrichmentStatus.FAILED;
    }

}
