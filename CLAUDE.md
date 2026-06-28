# CLAUDE.md — Qalam Android

Agent guidance for developing this app. Read before touching code.

## Project context

Personal Arabic learning app. Single user (Toni). Connects to a Ktor backend via Tailscale.
Full spec: `docs/android-app.md`. Design tokens: `docs/android-design.md`. Build plan: `docs/android-plan.md`.
Interactive prototype (visual source of truth): `wireframe/Qalam.dc.html`.

## Architecture — non-negotiable layer rules

```
UI (Composables + ViewModels)
    ↓
Repository interfaces  ← domain layer, zero Android dependencies
    ↓
Remote (ApiClient) + Local (Room DAOs)
```

- ViewModels expose `StateFlow<SealedUiState>` — one sealed class per screen: `Loading / Success / Error`
- Composables are stateless: observe state, dispatch events, no logic
- DTOs live in `data/api/dto/` — never leak into UI or domain
- Domain models live in `domain/model/` — pure Kotlin, no framework imports
- Hilt binds implementations in `di/RepositoryModule.kt`

## UI — non-negotiable design rules

**Palette:** warm parchment — see `docs/android-design.md` for all hex tokens. Do not substitute Material3 defaults.

**Fonts:**
- Hanken Grotesk → UI text
- Newsreader → prose and transliteration
- Amiri → all Arabic text

**Arabic text always:** `layoutDirection = LayoutDirection.Rtl`, Amiri font, `fontSize >= 24.sp` for body, larger for hero. Never use system default for Arabic.

**Mastery colors (exact):**
- Unseen `#A99F8B` · Learning `#B07D26` · Reviewing `#2F6E9E` · Mastered `#1F6F5C`

**Light mode only.** No dark theme. No `.dark` variants. No `isSystemInDarkTheme()` branches.

## Phase discipline

Implementation follows 8 phases defined in `docs/android-plan.md`. Each phase must build and run before starting the next. Do not implement features from phase N+1 while phase N has compile errors or broken navigation.

| Phase | Gate |
|-------|------|
| 0 | Project builds, fonts load, network config in place |
| 1 | Theme + bottom nav + empty placeholder screens |
| 2 | Settings screen, Ktor client wired, connection indicator |
| 3 | Word list + detail + quick-add |
| 4 | Root list + detail |
| 5 | Text list + interlinear reader |
| 6 | Training flashcard loop + session summary |
| 7 | Home screen (consumes all existing API calls) |
| 8 | Offline banner, Room cache, build polish |

## API contract

Backend: `http://<laptop>.ts.net:8080/api/v1/`. No auth (Tailscale is the perimeter).
Base URL from DataStore — never hardcoded.
DTOs: mirror from `GET /api/v1/openapi.json`. **Never hand-invent response types.**
Error envelope: `{ "error": "...", "code": "..." }`. Pagination: `{ "items": [], "total": N, "page": N, "size": N }`.

## Quality gates

Run before declaring any phase done:
```bash
./gradlew lint                  # Android lint
./gradlew detekt                # once detekt is configured (phase 0)
./gradlew test                  # unit tests
./gradlew connectedAndroidTest  # instrumented tests (device required)
```

Or via: `just check`

## What NOT to do

- Hand-invent DTO fields — always derive from OpenAPI spec
- Use Material3 color scheme defaults — palette is custom
- Add dark mode support
- Build features out of scope: creating/editing texts, roots, tokens, AI enrichment, export
- Add auth — Tailscale is the perimeter, none needed
- Skip Hilt for DI — no service locators, no manual object graphs
- Put business logic in Composables
- Hardcode the backend URL
