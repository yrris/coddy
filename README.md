# Coddy Backend

> Spring Boot backend for an AI-powered website generator — describe what you want, get a deployable site.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-green?logo=springboot)
![LangChain4j](https://img.shields.io/badge/LangChain4j-1.1-blue)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue?logo=postgresql)
![Redis](https://img.shields.io/badge/Redis-7-red?logo=redis)
![Docker](https://img.shields.io/badge/Docker-Compose-blue?logo=docker)

**Frontend repo:** [coddy-frontend](https://github.com/yrris/coddy-frontend)

---

## Features

- **AI Code Generation** — Send a natural language prompt, receive structured HTML/CSS/JS output via LangChain4j + OpenAI-compatible models
- **Streaming (SSE)** — Real-time token-level streaming over Server-Sent Events using Project Reactor Flux
- **Conversation Memory** — Per-project chat context stored in Redis with a 20-message sliding window and 7-day TTL
- **Two Generation Modes** — Single-file HTML or multi-file (HTML + CSS + JS), each with dedicated parsers and file savers
- **One-Click Deploy** — Generated sites are packaged and served via unique deploy keys
- **Authentication** — Email/password registration + Google OAuth 2.0, session-backed by Redis
- **Role-Based Access** — Custom `@AuthCheck` AOP annotation for user/admin endpoint protection
- **Chat History** — Cursor-based pagination on descending IDs for efficient history loading
- **Admin Panel** — Manage users, apps, featured content, and chat records
- **Monitoring** — Prometheus metrics endpoint via Micrometer Actuator
- **API Docs** — Knife4j / Swagger UI at `/api/swagger-ui.html`

## Architecture

```
┌─────────────────────────────────────────────────────────────────┐
│                        React Frontend                           │
│                  (SSE EventSource + REST)                        │
└──────────────────────────┬──────────────────────────────────────┘
                           │
┌──────────────────────────▼──────────────────────────────────────┐
│                    Spring Boot Backend                           │
│                                                                  │
│  ┌──────────────┐   ┌───────────────────┐   ┌───────────────┐  │
│  │  Controllers  │──▶│  AiCodeGenerator  │──▶│  LangChain4j  │  │
│  │  (REST/SSE)   │   │     Facade        │   │    Agent       │  │
│  └──────────────┘   └───────┬───────────┘   └───────┬───────┘  │
│                             │                        │           │
│                    ┌────────▼────────┐      ┌────────▼────────┐ │
│                    │  CodeParser     │      │  Chat Model     │ │
│                    │  (Strategy)     │      │  (OpenAI API)   │ │
│                    └────────┬────────┘      └─────────────────┘ │
│                             │                                    │
│                    ┌────────▼────────┐                           │
│                    │  CodeFileSaver  │                           │
│                    │  (Template)     │                           │
│                    └────────┬────────┘                           │
│                             │                                    │
│                    ┌────────▼────────┐                           │
│                    │  Deploy Service │                           │
│                    │  (Static Host)  │                           │
│                    └─────────────────┘                           │
│                                                                  │
│  ┌──────────┐   ┌────────────┐   ┌───────────────────────────┐  │
│  │ Security │   │  Flyway    │   │  Redis                    │  │
│  │ + OAuth2 │   │ Migrations │   │  (Session + Chat Memory)  │  │
│  └──────────┘   └────────────┘   └───────────────────────────┘  │
└──────────────────────────────────────────────────────────────────┘
                           │
              ┌────────────▼────────────┐
              │  PostgreSQL + Redis     │
              │  (Docker Compose)       │
              └─────────────────────────┘
```

### Code Generation Flow

```
User prompt
  → AiCodeGeneratorFacade
    → AiCodeGeneratorService (factory selects langchain4j or mock)
      → LangChain4j Agent (with Redis-backed conversation memory)
        → LLM generates structured JSON / streamed markdown
      → CodeParserExecutor (strategy: HTML_SINGLE or HTML_MULTI)
        → Parse response into file map
      → CodeFileSaverExecutor (template method)
        → Write files to tmp/code_output/{type}_{appId}
  → Response / SSE stream returned to client
```

## Getting Started

### Prerequisites

- Java 21+
- Docker & Docker Compose (for PostgreSQL and Redis)
- An OpenAI-compatible API key

### 1. Start Infrastructure

```bash
cd ../infra
docker compose up -d postgres redis
```

### 2. Configure Environment

```bash
cp .env.example .env
# Edit .env with your settings:
#   AI_PROVIDER=langchain4j
#   AI_API_KEY=sk-...         (required if using langchain4j)
#   AI_MODEL=gpt-5.4-mini
#   GOOGLE_AUTH_ENABLED=false (or true + client credentials)
```

### 3. Run the Backend

```bash
./mvnw spring-boot:run
```

The API is available at `http://localhost:8765/api`

### 4. Verify

```bash
curl http://localhost:8765/api/health/ping
```

### API Docs

Swagger UI: `http://localhost:8765/api/swagger-ui.html`

## Key API Endpoints

| Method | Path                            | Description                           |
| ------ | ------------------------------- | ------------------------------------- |
| POST   | `/api/user/register`            | Register with email/password          |
| POST   | `/api/user/login`               | Login                                 |
| GET    | `/api/user/current`             | Current session user                  |
| POST   | `/api/ai/codegen/generate`      | Synchronous code generation           |
| POST   | `/api/ai/codegen/stream`        | SSE streaming code generation         |
| POST   | `/api/app/add`                  | Create new app project                |
| GET    | `/api/app/chat/gen/code`        | Chat-based streaming generation (SSE) |
| GET    | `/api/app/{appId}/chat/history` | Cursor-paginated chat history         |
| POST   | `/api/app/deploy`               | Deploy app to static hosting          |
| GET    | `/api/deployed/{key}/...`       | Serve deployed site                   |
| GET    | `/api/actuator/prometheus`      | Prometheus metrics                    |

## Project Structure

```
coddy/
├── src/main/java/com/yrris/coddy/
│   ├── controller/          # REST + SSE endpoints
│   ├── model/               # JPA entities (AppUser, AppProject, ChatHistory)
│   ├── repository/          # Spring Data JPA repositories
│   ├── service/             # Business logic
│   ├── aop/                 # @AuthCheck annotation + aspect
│   ├── ai/
│   │   ├── agent/           # LangChain4j agent interface
│   │   ├── codegen/         # Code generation pipeline
│   │   │   ├── executor/    # Parser (strategy) + FileSaver (template)
│   │   │   └── facade/      # Orchestration layer
│   │   └── factory/         # Agent factory with Caffeine cache
│   ├── config/              # Security, Redis, CORS, AI properties
│   └── common/              # Shared DTOs, error codes, base response
├── src/main/resources/
│   ├── db/migration/        # Flyway SQL migrations
│   ├── prompts/             # LLM system prompts per generation mode
│   └── application*.yml     # Profile configs (local/dev/prod)
└── src/test/                # JUnit 5 tests
```

[//]: # (## Screenshots)

[//]: # ()
[//]: # (<!-- Add your own screenshots or GIFs here -->)

[//]: # ()
[//]: # (| Chat & Generation |   Live Preview    |    Admin Panel    |)

[//]: # (| :---------------: | :---------------: | :---------------: |)

[//]: # (| _screenshot here_ | _screenshot here_ | _screenshot here_ |)

## Planned Features

- Containerised build pipeline with sandboxed execution for React project generation
- Rate limiting and cost tracking per user for LLM API usage
- WebSocket upgrade for bidirectional streaming
- CI/CD with GitHub Actions and cloud deployment (AWS/GCP)

## Tech Stack Detail

| Layer           | Technology                              |
| --------------- | --------------------------------------- |
| Language        | Java 21 (virtual threads ready)         |
| Framework       | Spring Boot 3.5                         |
| AI/LLM          | LangChain4j 1.1 + OpenAI-compatible API |
| ORM             | Spring Data JPA / Hibernate             |
| Database        | PostgreSQL 16                           |
| Cache / Session | Redis 7, Spring Session                 |
| Migrations      | Flyway                                  |
| Auth            | Spring Security, OAuth2 Client (Google) |
| Monitoring      | Micrometer + Prometheus                 |
| API Docs        | Knife4j (OpenAPI 3)                     |
| Testing         | JUnit 5, H2 in-memory                   |
| Build           | Maven, Docker Compose                   |

## License

MIT
