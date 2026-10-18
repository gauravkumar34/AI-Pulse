package com.spring_ai.spring_ai.source;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;

@Entity
@Table(name = "sources")
public class Source {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "source_type", nullable = false, length = 30)
    private SourceType sourceType;

    @Column(name = "feed_url", nullable = false, unique = true)
    private String feedUrl;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "last_fetched_at")
    private OffsetDateTime lastFetchedAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    protected Source() {
    }

    public Source(String name, SourceType sourceType,
                  String feedUrl, boolean active) {
        this.name = name;
        this.sourceType = sourceType;
        this.feedUrl = feedUrl;
        this.active = active;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public SourceType getSourceType() {
        return sourceType;
    }

    public String getFeedUrl() {
        return feedUrl;
    }

    public boolean isActive() {
        return active;
    }

    public OffsetDateTime getLastFetchedAt() {
        return lastFetchedAt;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
