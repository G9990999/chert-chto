# MWS Wiki — Collaborative Knowledge Platform

A full-stack wiki editor deeply integrated with **MWS Tables**, enabling teams to create, link, and collaboratively edit rich-text pages that contain live table data.

---

## Architecture

```
┌────────────────────────────────────────────────────────────┐
│  Browser                                                   │
│  React 18 + TypeScript + TipTap (via Vite)                │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────────┐   │
│  │  Auth pages  │ │  Page list   │ │  Wiki editor     │   │
│  │  Login/Reg   │ │  + Search    │ │  TipTap + Slash  │   │
│  └──────────────┘ └──────────────┘ └──────────────────┘   │
│          │ HTTP/REST              │ WebSocket/STOMP        │
└──────────┼────────────────────────┼────────────────────────┘
           │                        │
┌──────────▼────────────────────────▼────────────────────────┐
│  Spring Boot 3.2.5 (Java 21, virtual threads)             │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Spring Security + JWT (ROLE_USER/MANAGER/ADMIN)    │  │
│  │  JwtAuthFilter → SecurityFilterChain                │  │
│  └──────────────────────────────────────────────────────┘  │
│                                                            │
│  ┌────────────┐ ┌────────────┐ ┌────────────────────────┐  │
│  │  /api/auth │ │ /api/pages │ │  /api/tables (proxy)   │  │
│  │  AuthCtrl  │ │  PageCtrl  │ │  TablesController      │  │
│  └────────────┘ └────────────┘ └────────────────────────┘  │
│                                                            │
│  ┌──────────────┐ ┌──────────────────────────────────────┐ │
│  │  WebSocket   │ │  MwsTablesClient                     │ │
│  │  /ws STOMP   │ │  CircuitBreaker + TimeLimiter        │ │
│  │  SimpleBroker│ │  Caffeine cache (5 min TTL)          │ │
│  └──────────────┘ └──────────────────────────────────────┘ │
│                                                            │
│  ┌──────────────────────────────────────────────────────┐  │
│  │  Spring Data JPA + Hibernate Envers (versioning)    │  │
│  │  Liquibase migrations                               │  │
│  └──────────────────────────────────────────────────────┘  │
└──────────────────────────┬─────────────────────────────────┘
                           │
                 ┌─────────▼──────────┐
                 │  PostgreSQL 17     │
                 │  (postgres:17-alp) │
                 └────────────────────┘
                           │ external
                 ┌─────────▼──────────┐
                 │  MWS Tables API    │
                 │  tables.mws.ru     │
                 └────────────────────┘
```

### Key Design Decisions

| Concern | Solution |
|---|---|
| Rich-text editing | TipTap (MIT licence, ProseMirror-based) |
| Collaborative editing | STOMP over SockJS (Spring SimpleMessageBroker) |
| Page versioning | Hibernate Envers `@Audited` — full revision history |
| Optimistic locking | JPA `@Version` field prevents silent overwrites |
| Local autosave | `localStorage` draft with 24 h TTL, cleared after server save |
| External API resilience | Resilience4j CircuitBreaker + TimeLimiter (5 s) |
| GET caching | Caffeine L1 cache (500 entries, 5 min TTL) |
| Role model | `USER` · `MANAGER` · `ADMIN` — enforced in Spring Security |
| DB migrations | Liquibase YAML changelogs |
| Virtual threads | `spring.threads.virtual.enabled=true` (Java 21 Loom) |

---

## Functional Overview

### Mandatory features

- **Insert MWS Tables datasheet** into a page body (live data, not a screenshot)
- **Inline autosave** to localStorage + debounced server sync (1.5 s)
- **Backlinks** — every page shows which other pages link to it
- **Slash-menu** (`/`) with keyboard navigation (↑↓ Enter Esc) for quick block insertion
- **Collaborative editing** — STOMP broadcasts edits to all connected editors in real time

### Additional features

- Hibernate Envers **revision history** — full audit trail of every save
- **Optimistic locking** — `@Version` field rejects stale concurrent writes
- **Role-based access** — `USER`, `MANAGER`, `ADMIN` with different permissions
- **Page sharing** — share individual pages with specific users
- **Public pages** — optionally visible to all authenticated users
- Circuit breaker with **fallback** when MWS Tables is unreachable
- **Virtual threads** for high-concurrency HTTP and WebSocket handling

---

## Repository Structure

```
.
├── back-end/               Spring Boot 3.2.5 backend
│   ├── src/main/java/ru/mws/wiki/
│   │   ├── config/         Security, WebSocket, Cache, WebClient config
│   │   ├── controller/     REST controllers (Auth, Page, Tables)
│   │   ├── client/         MwsTablesClient (WebClient + Resilience4j)
│   │   ├── dto/            Request/response records
│   │   ├── entity/         JPA entities (User, Page)
│   │   ├── exception/      Custom exceptions + GlobalExceptionHandler
│   │   ├── repository/     JPA repositories
│   │   ├── security/       JWT service + filter + UserDetailsService
│   │   ├── service/        Business logic (AuthService, PageService)
│   │   └── websocket/      STOMP CollaborationController
│   ├── src/main/resources/
│   │   ├── application.yml
│   │   └── db/changelog/   Liquibase YAML changelogs
│   ├── src/test/           Unit tests (AuthServiceTest)
│   ├── build.gradle        Groovy DSL
│   └── Dockerfile
├── front-end/              React 18 + TypeScript + TipTap (Vite)
│   ├── src/
│   │   ├── components/
│   │   │   ├── auth/       LoginPage, RegisterPage
│   │   │   ├── editor/     WikiEditor (TipTap), SlashMenu
│   │   │   ├── pages/      PagesList, PageView
│   │   │   └── tables/     TableEmbed (live MWS data)
│   │   ├── hooks/          useLocalCache (localStorage autosave)
│   │   ├── services/       api.ts (REST), websocket.ts (STOMP)
│   │   ├── store/          authStore (Zustand + persist)
│   │   ├── types/          TypeScript interfaces
│   │   ├── main.tsx        App shell + routing
│   │   └── styles.css      Global styles
│   ├── nginx.conf
│   └── Dockerfile
├── api/                    MWS Tables OpenAPI spec
│   └── api_tables.yml
├── docs/                   Additional documentation
├── docker-compose.yml
├── README.md
├── Roadmap.md
└── Backlog.md
```

---

## Installation & Setup

### Prerequisites

- Docker ≥ 24
- Docker Compose ≥ 2.20

### Quick start (Docker Compose)

```bash
git clone https://github.com/G9990999/chert-chto.git
cd chert-chto
docker compose up --build
```

The services will start in this order:
1. **PostgreSQL 17** — listens on port 5432
2. **Backend** — listens on port 8080 (waits for DB health check)
3. **Frontend** — listens on port 80 (Nginx, proxies /api and /ws)

Open **http://localhost** in your browser.

> **Note:** The first build compiles the Gradle project inside Docker and downloads npm packages — allow ~3-5 minutes.

### Development mode (hot reload)

**Backend:**
```bash
cd back-end
./gradlew bootRun
# Runs on http://localhost:8080
```

**Frontend:**
```bash
cd front-end
npm install
npm run dev
# Runs on http://localhost:3000 (proxies to backend on 8080)
```

**Database only:**
```bash
docker compose up postgres
```

---

## API Reference

### Authentication

| Method | Path | Role | Description |
|---|---|---|---|
| POST | `/api/auth/register` | Public | Register new user |
| POST | `/api/auth/login` | Public | Authenticate and get JWT |

### Pages

| Method | Path | Role | Description |
|---|---|---|---|
| GET | `/api/pages` | USER+ | List accessible pages |
| GET | `/api/pages/search?q=` | USER+ | Search pages by title |
| GET | `/api/pages/{id}` | USER+ | Get page with backlinks |
| POST | `/api/pages` | USER+ | Create new page |
| PUT | `/api/pages/{id}` | USER+ | Update page content |
| POST | `/api/pages/{id}/share` | MANAGER+ | Share page with users |
| DELETE | `/api/pages/{id}` | ADMIN | Delete page |

### MWS Tables (proxy)

| Method | Path | Description |
|---|---|---|
| GET | `/api/tables` | List datasheets in space |
| GET | `/api/tables/{dstId}/fields` | Get datasheet fields |
| GET | `/api/tables/{dstId}/records` | Get records (paginated) |
| GET | `/api/tables/{dstId}/views` | Get views |

### WebSocket / STOMP

- **Endpoint:** `ws://host/ws` (SockJS fallback enabled)
- **Subscribe:** `/topic/pages/{pageId}` — receive edit broadcasts
- **Publish:** `/app/pages/{pageId}/edit` — send edit events

---

## Environment Variables

| Variable | Default | Description |
|---|---|---|
| `DB_URL` | `jdbc:postgresql://postgres:5432/mwswiki` | JDBC URL |
| `DB_USER` | `mwswiki` | DB username |
| `DB_PASSWORD` | `mwswiki` | DB password |
| `JWT_SECRET` | *(see docker-compose)* | HMAC-SHA256 key (≥32 chars) |
| `MWS_TABLES_URL` | `https://tables.mws.ru/fusion/v1` | MWS Tables base URL |
| `MWS_TOKEN` | `uskTBmR1tIBRHQsNU1sNCH3` | MWS API bearer token |
| `MWS_SPACE_ID` | `spc9PARSkMd3V` | MWS workspace ID |

---

## Testing

### Backend unit tests

```bash
cd back-end
./gradlew test
```

Test reports: `back-end/build/reports/tests/test/index.html`

### Manual API testing

```bash
# Register
curl -X POST http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","email":"alice@example.com","password":"password123","displayName":"Alice"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"username":"alice","password":"password123"}'

# Create page (use token from login response)
curl -X POST http://localhost:8080/api/pages \
  -H 'Authorization: Bearer <token>' \
  -H 'Content-Type: application/json' \
  -d '{"title":"Hello","content":"{}","publicPage":false,"linkedPageIds":[]}'

# Get MWS Tables datasheets
curl http://localhost:8080/api/tables \
  -H 'Authorization: Bearer <token>'
```

---

## Role Permissions

| Action | USER | MANAGER | ADMIN |
|---|---|---|---|
| Register / Login | ✓ | ✓ | ✓ |
| View own/shared pages | ✓ | ✓ | ✓ |
| Create page | ✓ | ✓ | ✓ |
| Edit own page | ✓ | ✓ | ✓ |
| Share page | — | ✓ | ✓ |
| View all pages | — | ✓ | ✓ |
| Delete any page | — | — | ✓ |
| Admin endpoints | — | — | ✓ |
