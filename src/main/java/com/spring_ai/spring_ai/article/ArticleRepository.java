package com.spring_ai.spring_ai.article;

import com.spring_ai.spring_ai.enrichment.ArticleEnrichment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    boolean existsByCanonicalUrl(String canonicalUrl);
    List<Article> findTop50ByOrderByRetrievedAtDesc();

    long countByEnrichmentStatus(Article.EnrichmentStatus status);

    List<Article> findByEnrichmentStatusOrderByRetrievedAtAsc(
            Article.EnrichmentStatus status,
            Pageable pageable
    );

    @Modifying
    @Query(value = """
        DELETE FROM articles
        WHERE retrieved_at < :cutoff
        """, nativeQuery = true)
    int deleteExpiredArticles(@Param("cutoff") java.time.OffsetDateTime cutoff);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
    update Article a
       set a.enrichmentStatus = :to
     where a.id = :id and a.enrichmentStatus = :from
""")
    int transition(@Param("id") Long id,
                   @Param("from") Article.EnrichmentStatus from,
                   @Param("to") Article.EnrichmentStatus to);

}