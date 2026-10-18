
-- AI Pulse: initial database schema

CREATE TABLE sources (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(200) NOT NULL,
                         source_type VARCHAR(30) NOT NULL
                             CHECK (source_type IN (
                                                    'RSS', 'GITHUB', 'BLOG', 'RESEARCH', 'SECURITY'
                                 )),
                         feed_url TEXT NOT NULL UNIQUE,
                         is_active BOOLEAN NOT NULL DEFAULT TRUE,
                         last_fetched_at TIMESTAMPTZ,
                         created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                         updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE articles (
                          id BIGSERIAL PRIMARY KEY,
                          source_id BIGINT NOT NULL
                              REFERENCES sources(id) ON DELETE CASCADE,

                          external_id TEXT,
                          canonical_url TEXT NOT NULL UNIQUE,
                          title TEXT NOT NULL,
                          author TEXT,
                          published_at TIMESTAMPTZ,
                          retrieved_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          content TEXT,
                          content_hash VARCHAR(64),

                          enrichment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING'
                              CHECK (enrichment_status IN (
                                                           'PENDING', 'PROCESSING', 'COMPLETED', 'FAILED'
                                  )),

                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                          UNIQUE (source_id, external_id)
);

CREATE INDEX idx_articles_retrieved_at
    ON articles (retrieved_at);

CREATE INDEX idx_articles_published_at
    ON articles (published_at DESC);

CREATE INDEX idx_articles_source_id
    ON articles (source_id);

CREATE INDEX idx_articles_enrichment_status
    ON articles (enrichment_status);

CREATE TABLE article_enrichments (
                                     id BIGSERIAL PRIMARY KEY,
                                     article_id BIGINT NOT NULL
                                         REFERENCES articles(id) ON DELETE CASCADE,

                                     version INTEGER NOT NULL CHECK (version > 0),
                                     model_name VARCHAR(100) NOT NULL,
                                     prompt_version VARCHAR(50) NOT NULL,

                                     summary TEXT NOT NULL,
                                     background TEXT,
                                     what_changed TEXT,
                                     why_it_matters TEXT,
                                     limitations TEXT,
                                     developer_impact TEXT,

                                     primary_category VARCHAR(40) NOT NULL
                                         CHECK (primary_category IN (
                                                                     'MODELS_RELEASES',
                                                                     'AGENTS_MCP',
                                                                     'DEVELOPER_TOOLS',
                                                                     'RESEARCH_TECHNIQUES',
                                                                     'SECURITY_RELIABILITY'
                                             )),

                                     importance_score SMALLINT NOT NULL
                                         CHECK (importance_score BETWEEN 1 AND 5),
                                     importance_reason TEXT,

                                     is_current BOOLEAN NOT NULL DEFAULT TRUE,
                                     created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                                     UNIQUE (article_id, version)
);

-- Only one enrichment version can be current per article.
CREATE UNIQUE INDEX uq_article_current_enrichment
    ON article_enrichments (article_id)
    WHERE is_current = TRUE;

CREATE TABLE article_tags (
                              id BIGSERIAL PRIMARY KEY,
                              article_id BIGINT NOT NULL
                                  REFERENCES articles(id) ON DELETE CASCADE,
                              tag VARCHAR(80) NOT NULL,
                              created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),

                              UNIQUE (article_id, tag)
);

CREATE INDEX idx_article_tags_tag
    ON article_tags (tag);
