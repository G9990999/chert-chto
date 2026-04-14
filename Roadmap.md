# Roadmap

## v1.0 — Hackathon MVP (current)
- [x] TipTap rich-text editor with StarterKit extensions
- [x] Slash-menu with keyboard navigation
- [x] MWS Tables live embed (fields + records via proxy API)
- [x] Inline autosave (localStorage + debounced server sync)
- [x] Backlinks and outgoing page links
- [x] Real-time collaborative editing via STOMP/WebSocket
- [x] JWT authentication with role model (USER/MANAGER/ADMIN)
- [x] Page sharing (per-user and public)
- [x] Hibernate Envers revision history
- [x] Resilience4j circuit breaker + time limiter for MWS Tables API
- [x] Caffeine L1 cache for GET endpoints
- [x] Virtual threads (Java 21 Loom)
- [x] Docker Compose one-command deployment

## v1.1 — Post-hackathon polish
- [ ] Operational Transformation or CRDT (e.g. Yjs) for true conflict-free concurrent edits
- [ ] Page history UI — browse and restore previous revisions from Envers audit log
- [ ] Comment threads on specific paragraphs
- [ ] Graph view of page links (D3.js or Cytoscape)
- [ ] Full-text search with PostgreSQL `tsvector` and trigram indexes
- [ ] Drag-and-drop file/image upload with S3-compatible storage
- [ ] Notifications (WebSocket push) for mentions and page changes

## v1.2 — Platform integration
- [ ] MWS Tables write-back — edit table records directly from the wiki page
- [ ] Two-way sync: changes to a linked table row trigger a page update event
- [ ] Webhooks for external integrations
- [ ] OpenAPI documentation (Springdoc)
- [ ] i18n (EN/RU)

## v2.0 — Enterprise features
- [ ] AI writing assistant (summarise, expand, translate using Claude API)
- [ ] Pluggable extension system for custom block types
- [ ] Workspace-level permissions and SSO (OAuth2 / SAML)
- [ ] Export to PDF, Markdown, Confluence
- [ ] Analytics dashboard (page views, edit frequency)
