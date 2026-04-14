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

## Bug Fixes Applied (Issue #3 — Test Environment Setup)

| # | File | Bug | Fix |
|---|---|---|---|
| F-01 | `front-end/package.json` | `@tiptap/extension-slash-command` listed as dependency does not exist on npm — `npm install` fails entirely | Removed non-existent package; the slash menu is custom-implemented in `SlashMenu.tsx` |
| F-02 | `front-end/package.json` | `@tiptap/extension-typography`, `@tiptap/extension-character-count`, `@tiptap/extension-collaboration`, `@tiptap/extension-code-block-lowlight`, `@tiptap/pm`, `lowlight` listed but never imported in source code — dead dependencies | Removed all unused TipTap packages to clean up `package.json` |
| F-03 | `front-end/public/index.html` | Vite entry-point `index.html` was placed inside `public/` instead of the project root — `npm run build` fails with "Could not resolve entry module index.html" | Moved `index.html` from `public/` to the project root |
| F-04 | `front-end/src/components/editor/SlashMenu.tsx`, `WikiEditor.tsx`, `pages/PageView.tsx`, `pages/PagesList.tsx` | `import React` present in files using `react-jsx` transform with `noUnusedLocals: true` — TypeScript build fails with TS6133 "React is declared but its value is never read" | Removed `React` from the default import in the four affected files |
| F-05 | `front-end/src/components/tables/TableEmbed.tsx` | Local `interface Record` shadows the built-in `Record<K,V>` generic type — TypeScript build fails with TS2315 "Type 'Record' is not generic" | Renamed the local interface to `TableRow` |
| F-06 | `front-end/` | No `.eslintrc` config file present even though ESLint and its TypeScript plugin are listed in `devDependencies` and a `lint` script is defined — `npm run lint` crashes with "ESLint couldn't find a configuration file" | Created `.eslintrc.cjs` with recommended ESLint + TypeScript-ESLint rules |
| F-07 | `back-end/src/main/java/ru/mws/wiki/entity/Page.java` | Lombok `@Builder` ignores field initializers for `sharedWith` and `linkedPageIds` (`= new HashSet<>()`), so builder-constructed instances have `null` collections — causes `NullPointerException` at runtime when `getSharedWith().stream()` or `getSharedWith().addAll()` is called | Added `@Builder.Default` on both collection fields |
| F-08 | `back-end/src/main/java/ru/mws/wiki/entity/User.java` | Same `@Builder` / `@Builder.Default` issue for `ownedPages` and `sharedPages` fields | Added `@Builder.Default` on both collection fields |
| F-09 | `back-end/src/main/java/ru/mws/wiki/client/MwsTablesClient.java` | `@Cacheable` key `"'datasheets-' + #spaceId"` references `#spaceId` which is an instance field, not a method parameter — SpEL resolves it to `null`, so the cache key becomes `"datasheets-null"` regardless of space | Changed key to the literal string `"'datasheets'"` |
| F-10 | `back-end/` | `gradlew` wrapper and `gradle/wrapper/` directory missing — Docker build (`COPY gradlew .` / `RUN ./gradlew bootJar`) would fail immediately | Generated Gradle 8.7 wrapper via `gradle wrapper` task |
| F-11 | `front-end/src/components/pages/PageView.tsx` | `wsReady` ternary rendered identical `<WikiEditor>` in both branches — dead conditional, always renders the same component | Replaced the redundant ternary with a single unconditional `<WikiEditor>` render |

## Known Limitations (MVP)

| # | Limitation | Mitigation |
|---|---|---|
| L-01 | Last-write-wins on concurrent edits | CRDT (B-01) in v1.1 |
| L-02 | No soft delete — hard delete only | Add `deletedAt` column + restore endpoint |
| L-03 | No rate limiting on auth endpoints | Add Spring Security's built-in throttling |
| L-04 | Caffeine cache not distributed | Replace with Redis for multi-instance deployments |
| L-05 | SimpleMessageBroker not horizontally scalable | Replace with RabbitMQ STOMP adapter |
