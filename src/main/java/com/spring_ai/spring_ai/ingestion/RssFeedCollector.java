package com.spring_ai.spring_ai.ingestion;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import com.spring_ai.spring_ai.article.Article;
import com.spring_ai.spring_ai.article.ArticleRepository;
import com.spring_ai.spring_ai.source.Source;
import com.spring_ai.spring_ai.source.SourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URLConnection;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Date;

@Service
public class RssFeedCollector {

    private final SourceRepository sourceRepository;
    private final ArticleRepository articleRepository;

    public RssFeedCollector(
            SourceRepository sourceRepository,
            ArticleRepository articleRepository) {
        this.sourceRepository = sourceRepository;
        this.articleRepository = articleRepository;
    }

    @Transactional
    public int collectAll() throws Exception {
        int saved = 0;

        for (Source source : sourceRepository.findByActiveTrue()) {
            saved += collectSource(source);
        }

        return saved;
    }

    private int collectSource(Source source) throws Exception {
        URI uri = URI.create(source.getFeedUrl());

        URLConnection connection = uri.toURL().openConnection();
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(10000);
        connection.setRequestProperty(
                "User-Agent", "AI-Pulse/1.0 RSS Reader");

        try (XmlReader reader = new XmlReader(connection.getInputStream())) {
            SyndFeed feed = new SyndFeedInput().build(reader);
            int saved = 0;

            for (SyndEntry entry : feed.getEntries()) {
                String link = entry.getLink();

                if (link == null || link.isBlank()) {
                    continue;
                }

                String canonicalUrl = link.trim();

                if (articleRepository.existsByCanonicalUrl(canonicalUrl)) {
                    continue;
                }

                Date publishedDate = entry.getPublishedDate();

                OffsetDateTime publishedAt = publishedDate == null
                        ? null
                        : publishedDate.toInstant()
                        .atOffset(ZoneOffset.UTC);

                String content = entry.getDescription() == null
                        ? null
                        : entry.getDescription().getValue();

                Article article = new Article(
                        source,
                        entry.getUri(),
                        canonicalUrl,
                        entry.getTitle() == null
                                ? "(Untitled)"
                                : entry.getTitle(),
                        entry.getAuthor(),
                        publishedAt,
                        content
                );

                articleRepository.save(article);
                saved++;
            }

            return saved;
        }
    }
}

