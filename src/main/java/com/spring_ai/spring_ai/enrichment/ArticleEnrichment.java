package com.spring_ai.spring_ai.enrichment;

import com.spring_ai.spring_ai.article.Article;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.OffsetDateTime;
import java.util.Locale;

@Entity
@Table(
        name = "article_enrichments",
        uniqueConstraints = @UniqueConstraint(
                name = "unique_article_enrichment_version",
                columnNames = {"article_id", "version"}
        )
)
public class ArticleEnrichment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false)
    private Article article;

    @Column(nullable = false)
    private int version;

    @Column(name = "model_name", nullable = false, length = 100)
    private String modelName;

    @Column(name = "prompt_version", nullable = false, length = 50)
    private String promptVersion;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String background;

    @Column(name = "what_changed", columnDefinition = "TEXT")
    private String whatChanged;

    @Column(name = "why_it_matters", columnDefinition = "TEXT")
    private String whyItMatters;

    @Column(columnDefinition = "TEXT")
    private String limitations;

    @Column(name = "developer_impact", columnDefinition = "TEXT")
    private String developerImpact;

    @Column(name = "primary_category", nullable = false, length = 40)
    private String primaryCategory;

    @Column(name = "importance_score", nullable = false)
    private Short importanceScore;

    @Column(name = "importance_reason", columnDefinition = "TEXT")
    private String importanceReason;

    @Column(name = "is_current", nullable = false)
    private boolean current = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    protected ArticleEnrichment() {}

    public ArticleEnrichment(
            Article article,
            ArticleEnrichmentResult result,
            String modelName,
            int version
    ) {
        this.article = article;
        this.summary = result.summary();
        this.background = result.background();
        this.whatChanged = result.whatChanged();
        this.whyItMatters = result.whyItMatters();
        this.limitations = result.limitations();
        this.developerImpact = result.developerImpact();
        this.primaryCategory = result.primaryCategory().trim().toUpperCase(Locale.ROOT);
        this.importanceScore = (short) result.importanceScore();
        this.importanceReason = result.importanceReason();
        this.modelName = modelName;
        this.promptVersion = "v1";
        this.version = version;
    }

    public Long getId() { return id; }
    public Article getArticle() { return article; }
    public String getSummary() { return summary; }
    public String getBackground() { return background; }
    public String getWhatChanged() { return whatChanged; }
    public String getWhyItMatters() { return whyItMatters; }
    public String getLimitations() { return limitations; }
    public String getDeveloperImpact() { return developerImpact; }
    public String getPrimaryCategory() { return primaryCategory; }
    public Short getImportanceScore() { return importanceScore; }
    public String getImportanceReason() { return importanceReason; }
    public String getModelName() { return modelName; }
    public boolean isCurrent() { return current; }

}
