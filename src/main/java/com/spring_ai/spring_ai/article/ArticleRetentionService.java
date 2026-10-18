
package com.spring_ai.spring_ai.article;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class ArticleRetentionService {

    private final ArticleRepository articleRepository;

    public ArticleRetentionService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
    }

    @Scheduled(fixedDelay = 3600000)
    @Transactional
    public void deleteExpiredArticles() {
        OffsetDateTime cutoff = OffsetDateTime.now().minusHours(48);

        int deleted = articleRepository.deleteExpiredArticles(cutoff);

        if (deleted > 0) {
            System.out.println(
                    "AI Pulse retention: deleted "
                            + deleted + " expired articles."
            );
        }
    }
}
