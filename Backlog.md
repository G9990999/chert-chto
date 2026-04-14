# Backlog

## High Priority

| # | Item | Description | Effort |
|---|---|---|---|
| B-01 | CRDT collaborative editing | Replace last-write-wins broadcast with Yjs for true concurrent safety | L |
| B-02 | Page history UI | Browse Envers revisions, diff two versions side-by-side, restore | M |
| B-03 | Full-text search | PostgreSQL `tsvector` + `ts_rank`, exposed via `/api/pages/search?fulltext=` | M |
| B-04 | File upload | Image/file drag-and-drop in editor, stored in S3-compatible backend | M |
| B-05 | MWS Tables write-back | Allow editing table cells from within the wiki page embed | L |

## Medium Priority

| # | Item | Description | Effort |
|---|---|---|---|
| B-06 | Comment threads | Inline comments anchored to TipTap node marks | M |
| B-07 | Graph view | Interactive page-link graph (D3.js force layout) | M |
| B-08 | Notification system | WebSocket push for @mentions and watched-page changes | M |
| B-09 | OpenAPI docs | Springdoc integration, `/swagger-ui.html` | S |
| B-10 | Admin panel | User management UI for ADMIN role | M |
| B-11 | Export | Page → PDF (WeasyPrint or Prince) and Markdown | S |

## Low Priority / Nice to Have

| # | Item | Description | Effort |
|---|---|---|---|
| B-12 | AI assistant | Summarise, expand, translate selection via Claude API | M |
| B-13 | Dark mode | CSS custom property theming, persisted preference | S |
| B-14 | i18n | EN/RU translations (react-i18next) | S |
| B-15 | Webhooks | Outgoing webhooks on page create/update/delete | M |
| B-16 | SSO | OAuth2 login with Google / Yandex ID | L |
| B-17 | Analytics | Page-view counters, heatmaps, export to CSV | M |
| B-18 | Plugin API | Iframe sandboxed custom block types for third-party extensions | L |
| B-19 | Mobile app | React Native wrapper sharing editor logic | XL |

## Known Limitations (MVP)

| # | Limitation | Mitigation |
|---|---|---|
| L-01 | Last-write-wins on concurrent edits | CRDT (B-01) in v1.1 |
| L-02 | No soft delete — hard delete only | Add `deletedAt` column + restore endpoint |
| L-03 | No rate limiting on auth endpoints | Add Spring Security's built-in throttling |
| L-04 | Caffeine cache not distributed | Replace with Redis for multi-instance deployments |
| L-05 | SimpleMessageBroker not horizontally scalable | Replace with RabbitMQ STOMP adapter |
