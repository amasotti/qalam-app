# Qalam Android — Architecture

Reference for how the app is structured, the backend contract it depends on, and the non-obvious
decisions taken during the build. For visual tokens see [`design-system.md`](design-system.md).

## Connectivity model

| | |
|---|---|
| Transport | Tailscale (phone + backend host both enrolled) |
| Backend | Ktor at the host's Tailscale address; base URL stored in DataStore, editable in Settings |
| Default URL | Raw Tailscale IP (`PreferencesRepository.DEFAULT_URL`) — override in Settings |
| Auth | None — Tailscale is the security perimeter, single user |
| Cleartext | Plain HTTP permitted app-wide via `res/xml/network_security_config.xml`; domain-config is insufficient for raw Tailscale IPs |
| Health check | `GET /health` (Settings "Test connection" + Home connection pill) |

## Layer rules (Clean/Onion, mirrors the backend)

```
UI  (Composables + ViewModels)
      ↓
Repository interfaces   ← domain layer, zero Android dependencies
      ↓
Remote (ApiClient)      (+ Local/Room — not yet implemented)
```

- **ViewModels** expose one sealed `UiState` per screen as `StateFlow` (`Loading / Success / Error`).
  Private `_x: MutableStateFlow`, public `x: StateFlow` — never expose mutable state.
- **Composables** are stateless: observe state, dispatch events, no business logic.
- **DTOs** live in `data/api/dto/`, mapped to domain via `toDomain()`; never leak into UI/domain.
- **Domain models** live in `domain/model/` — pure Kotlin, no framework imports.
- **DI** is Hilt-only. `@HiltAndroidApp` on `QalamApp`, `@AndroidEntryPoint` on `MainActivity`
  (required for `hiltViewModel()`). Repository bindings in `di/RepositoryModule.kt`.
- **Base URL** comes from DataStore and is passed to `ApiClient` calls per-invocation — never injected
  into the `HttpClient`, never hardcoded.

### Navigation

Navigation3 (`androidx.navigation3`), **not** `navigation-compose` (maintenance mode) and **not**
`@Serializable` routes. A `Destination` sealed interface (`navigation/Destination.kt`) with
`data object` / `data class` entries; the back stack is a `SnapshotStateList<Any>` owned by
`MainActivity`. Screens receive typed nav lambdas and never touch the back stack directly.
`enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(...))` is required, otherwise system icons are
invisible on the parchment background.

## Backend API contract

All JSON, all under the base URL. Pagination envelope `{ items, total, page, size }`; error envelope
`{ error, code }`. The backend OpenAPI spec is the only source of truth for field names and shapes —
never hand-invent types. (Dev spec: `http://localhost:8085/api/v1/swagger-ui/documentation.yaml`.)

| Area | Endpoints |
|---|---|
| Health | `GET /health` |
| Words | `GET /api/v1/words` (`q`, `masteryLevel`, `rootId`, `sortBy`, `page`, `size`) · `GET /words/{id}` · `GET /words/by-arabic` · `POST /words` |
| Word examples | `GET /words/{id}/examples` · `POST /words/{id}/examples` · `POST /words/{id}/examples/generate` (AI) |
| Word links | `GET /words/{id}/dictionary-links` |
| Word lists | `GET/POST /api/v1/word-lists` · `GET/DELETE /word-lists/{id}` · `POST /word-lists/{id}/words` · `DELETE /word-lists/{id}/words/{wordId}` · `POST /word-lists/{id}/suggest` (AI) |
| Roots | `GET /api/v1/roots` · `GET /roots/{id}` (form count derived from a root-filtered word list) |
| Texts | `GET /api/v1/texts` · `GET /texts/{id}` · `GET /texts/{id}/sentences` · `GET/POST /texts/{id}/annotations` |
| Training | `POST /api/v1/training/sessions` (`mode`, `size`, optional `wordListIds`) · `POST /sessions/{id}/results` · `POST /sessions/{id}/complete` |
| Analytics | `GET /api/v1/analytics/overview` |
| AI insight | `POST /api/v1/ai/insight` (`entityType`, `entityId`, `mode`) |

AI endpoints return `503` when the AI service is down; the app maps this to
`AiUnavailableException` and shows a graceful fallback instead of an error.

## Key decisions

- **Navigation3 over navigation-compose** — the classic Compose navigation library is in maintenance
  mode; Navigation3's explicit back-stack model fits a small single-activity app cleanly.
- **Quick-add keeps translation required** — the backend `CreateWordRequest` only requires
  `arabicText`, but the learner workflow ("heard a word on the bus") wants the translation captured up
  front, so the UI enforces it.
- **Roots family is composed client-side** — `GET /roots/{id}` returns only root metadata, so the
  derivation family and form counts are built by combining it with a root-filtered word list.
- **Text reader tolerates empty sentences** — a newly created text legitimately has no sentences; that
  renders as empty interlinear content, but a *failed* sentence fetch is surfaced as an error, not
  silently shown as empty.
- **`dueCount` is derived** — the backend has no dedicated "due" queue, so Home computes
  `dueCount = totalWords − masteryCounts[MASTERED]` (everything still cycling through
  NEW/LEARNING/KNOWN). Home fetches analytics + recent words in parallel via `async`.
- **Mastery levels map to the backend enum** `NEW / LEARNING / KNOWN / MASTERED`.

## Not yet implemented

- **Offline / Room cache.** Repositories are remote-only; there is no local persistence or offline
  banner yet. The layer diagram leaves room for a Local source, and DTO shapes are stable enough to
  mirror into Room entities when offline becomes painful.
- **Release hardening** beyond the signed-build runbook (see
  [`runbooks/release-sideloading.md`](runbooks/release-sideloading.md)).
