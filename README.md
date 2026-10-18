# AI Pulse — Backend

**AI Pulse** is a local-first Developer Intelligence Platform that collects AI news and technical updates, enriches them using a local language model, and exposes structured APIs for discovering important developments.

The backend is built with Java, Spring Boot, Spring AI, PostgreSQL, and Ollama.

## Features

- **RSS ingestion:** Collect articles from configured RSS feeds.
- **Article persistence:** Store source information and article content in PostgreSQL.
- **AI enrichment:** Generate summaries, background, key changes, developer impact, limitations, categories, and importance scores using Qwen3.
- **Discover feed:** Return enriched articles ranked by importance.
- **Category filtering:** Filter stories by topic.
- **Article details:** Retrieve article metadata and AI-generated analysis.
- **Batch enrichment:** Process pending articles in configurable batches.
- **Automatic retention:** Delete articles older than 48 hours, including dependent enrichments and tags through database cascading.
- **Local-first AI:** Run inference through Ollama instead of requiring a hosted AI API.

## Technology Stack

| Component | Technology |
|---|---|
| Language | Java 17 |
| Backend framework | Spring Boot 4.1.1 |
| AI integration | Spring AI |
| Local language model | Ollama — `qwen3:8b` |
| Database | PostgreSQL 16 |
| Persistence | Spring Data JPA / Hibernate |
| Database migrations | Flyway |
| RSS parsing | Rome |
| API style | REST / JSON |
| Build tool | Maven |

## Architecture

```text
RSS Sources
    |
    v
RSS Feed Collector
    |
    v
Article Normalization & Deduplication
    |
    v
PostgreSQL
    |
    v
Article Enrichment Service
    |
    v
Spring AI --> Ollama (Qwen3)
    |
    v
Enrichment Results in PostgreSQL
    |
    v
REST API
    |
    v
Angular Frontend (planned)
```

The application uses a modular monolith architecture. Ingestion, article management, source configuration, and AI enrichment are separated into application packages while running in one Spring Boot service.

## Project Structure

```text
spring-ai/
├── src/
│   ├── main/
│   │   ├── java/com/spring_ai/spring_ai/
│   │   │   ├── SpringAiApplication.java
│   │   │   ├── article/
│   │   │   │   ├── Article.java
│   │   │   │   ├── ArticleRepository.java
│   │   │   │   ├── ArticleController.java
│   │   │   │   ├── ArticleRetentionService.java
│   │   │   │   └── dto/
│   │   │   ├── enrichment/
│   │   │   │   ├── ArticleEnrichment.java
│   │   │   │   ├── ArticleEnrichmentRepository.java
│   │   │   │   ├── ArticleEnrichmentService.java
│   │   │   │   └── ArticleEnrichmentResult.java
│   │   │   ├── ingestion/
│   │   │   │   ├── RssFeedCollector.java
│   │   │   │   └── IngestionController.java
│   │   │   └── source/
│   │   │       ├── Source.java
│   │   │       ├── SourceRepository.java
│   │   │       ├── SourceService.java
│   │   │       ├── SourceController.java
│   │   │       └── dto/
│   │   └── resources/
│   │       ├── application.properties
│   │       └── db/migration/
│   │           └── V1__create_core_tables.sql
│   └── test/
├── pom.xml
└── README.md
```

*This structure documents the implemented modules; adjust filenames if your local project differs.*

## Prerequisites

Install the following:

- Java 17
- Maven, or use the Maven wrapper if the project includes one
- PostgreSQL 16
- Ollama
- Git (optional)

Check your Java installation:

```bash
java -version
```

Check Maven:

```bash
mvn -version
```

## 1. Configure PostgreSQL

Create the database:

```sql
CREATE DATABASE ai_pulse;
```

Configure your database credentials in `src/main/resources/application.properties`.

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/ai_pulse
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false

spring.ai.ollama.base-url=http://localhost:11434
spring.ai.ollama.chat.options.model=qwen3:8b
```

Set the environment variables in your terminal before starting the application:

```bash
export DB_USERNAME=your_postgres_username
export DB_PASSWORD=your_postgres_password
```

Replace the example values with your local PostgreSQL credentials. Do not commit passwords or other secrets to Git.

Flyway applies database migrations at startup. Hibernate uses `validate` to check the entity mappings against the schema rather than modifying the schema automatically.

## 2. Configure Ollama

Install Ollama, then download the model:

```bash
ollama pull qwen3:8b
```

Start the Ollama service if it is not already running:

```bash
ollama serve
```

In another terminal, verify that the model responds:

```bash
ollama run qwen3:8b
```

Exit the interactive model session after testing. The Spring AI integration uses Ollama at `http://localhost:11434`.

## 3. Start the Backend

From the project root:

```bash
mvn spring-boot:run
```

Alternatively, if the project has a Maven wrapper:

```bash
./mvnw spring-boot:run
```

The API is available at:

```text
http://localhost:8080
```

## REST API Reference

All request and response bodies use JSON unless otherwise indicated.

### Source Management

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/sources` | Create an RSS source |
| `GET` | `/api/sources` | List configured sources |
| `GET` | `/api/sources/active` | List active sources |

Example request:

```http
POST /api/sources
Content-Type: application/json
```

```json
{
  "name": "OpenAI News",
  "sourceType": "BLOG",
  "feedUrl": "https://openai.com/news/rss.xml",
  "active": true
}
```

Supported source types in the current model:

- `RSS`
- `GITHUB`
- `BLOG`
- `RESEARCH`
- `SECURITY`

The current collector is RSS-focused; the other source types are available for future collectors.

### RSS Ingestion

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/ingestion/run` | Collect articles from active RSS sources |

Example:

```bash
curl -X POST http://localhost:8080/api/ingestion/run
```

The response reports the ingestion status and number of newly saved articles.

The collector skips entries without a usable link and avoids inserting articles whose canonical URLs already exist.

### Article APIs

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/articles` | List the latest articles |
| `GET` | `/api/articles/{id}` | Retrieve article details and enrichment |
| `GET` | `/api/articles/discover` | List enriched articles ranked by importance |
| `GET` | `/api/articles/discover?category={category}` | Filter the Discover feed by category |

Example:

```bash
curl http://localhost:8080/api/articles
```

Get a single article:

```bash
curl http://localhost:8080/api/articles/1
```

Get the Discover feed:

```bash
curl http://localhost:8080/api/articles/discover
```

Filter the Discover feed:

```bash
curl "http://localhost:8080/api/articles/discover?category=AGENTS_MCP"
```

Supported enrichment categories:

- `MODELS_RELEASES`
- `AGENTS_MCP`
- `DEVELOPER_TOOLS`
- `RESEARCH_TECHNIQUES`
- `SECURITY_RELIABILITY`

The regular article list can include articles that have not yet been enriched. The Discover feed contains articles with current enrichment records.

### AI Enrichment

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/enrichment/run?batchSize=5` | Enrich pending articles in a batch |
| `POST` | `/api/enrichment/{articleId}` | Enrich a specific article |

Run batch enrichment:

```bash
curl -X POST "http://localhost:8080/api/enrichment/run?batchSize=5"
```

Enrich a specific article:

```bash
curl -X POST http://localhost:8080/api/enrichment/1
```

The batch size is currently restricted to 1–5 articles per request.

The enrichment service uses Qwen3 to generate structured information such as:

- Summary
- Background
- What changed
- Why it matters
- Limitations
- Developer impact
- Primary category
- Importance score from 1 to 5
- Reason for the importance score

The service validates the generated category and score before saving the enrichment.

## Database Schema

Flyway migration `V1__create_core_tables.sql` creates the core tables.

### `sources`

Stores source configuration, including its name, type, feed URL, active status, and collection timestamps.

### `articles`

Stores article metadata and collected content, including:

- Source relationship
- External ID and canonical URL
- Title, author, and publication timestamp
- Retrieval timestamp
- Content and content hash
- Enrichment status

### `article_enrichments`

Stores AI-generated analysis and model provenance, including:

- Article relationship and version
- Model name and prompt version
- Summary and analysis fields
- Category and importance score
- Importance reason
- Current-version flag and creation timestamp

### `article_tags`

Stores tags associated with articles.

Article enrichment and tag records use cascading foreign-key deletion from their parent article.

## Retention Policy

AI Pulse is configured to retain article data for 48 hours based on `retrieved_at`.

The scheduled retention service runs periodically and deletes expired articles. Their dependent enrichment and tag rows are removed through database cascading. Source configuration is preserved.

The policy is based on retrieval time, not publication time. An old article newly collected from an RSS feed can therefore remain for up to 48 hours after collection.

## Development Workflow

A typical local workflow is:

1. Start PostgreSQL.
2. Start Ollama and ensure `qwen3:8b` is available.
3. Start Spring Boot.
4. Register an active RSS source.
5. Trigger RSS ingestion.
6. Run article enrichment in small batches.
7. Inspect the Discover feed and individual article endpoints.

For example:

```bash
curl -X POST http://localhost:8080/api/ingestion/run

curl -X POST \
 
