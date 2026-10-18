package com.spring_ai.spring_ai.enrichment;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ArticleEnrichmentRepository
        extends JpaRepository<ArticleEnrichment, Long> {

    Optional<ArticleEnrichment> findFirstByArticleIdAndCurrentTrue(Long articleId);

    int countByArticleId(Long articleId);
    List<ArticleEnrichment> findByCurrentTrueAndArticleIdIn(
            List<Long> articleIds
    );

    List<ArticleEnrichment> findByCurrentTrueOrderByImportanceScoreDescCreatedAtDesc(
            Pageable pageable
    );

    List<ArticleEnrichment>
    findByCurrentTrueAndPrimaryCategoryOrderByImportanceScoreDescCreatedAtDesc(
            String primaryCategory,
            Pageable pageable
    );

    @Modifying
    @Query("update ArticleEnrichment e set e.current = false " +
            "where e.article.id = :articleId and e.current = true")
    int clearCurrent(@Param("articleId") Long articleId);
}