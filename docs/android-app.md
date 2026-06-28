# Qalam Android App — Spec

## What this is

Native Android companion to Qalam (Arabic learning tool). Read-heavy, training-focused.
Connects to the existing Ktor backend over Tailscale. No cloud deployment. No Play Store.
Single user (Toni). Sideloaded APK via ADB.

Separate repository: `qalam-android` (public repo, private use).

Companion documents:
- `android-design.md` — colors, fonts, spacing, animations — source of truth from prototype
- `android-plan.md` — phased implementation guide for the build process

---

## Connectivity model

| | |
|---|---|
| Transport | Tailscale VPN (phone + laptop both enrolled) |
| Backend | Existing Ktor at `http://<laptop>.ts.net:8080` |
| Base URL | Configurable in app Settings screen, stored in DataStore |
| Auth | None — Tailscale is the perimeter, single user |
| Offline | Graceful degradation: show offline banner, cached Room data where available |

No new backend endpoints needed. All features use existing `/api/v1/` routes.

---

## Features in scope

### Home
- Greeting (time-aware) + app wordmark
- Connection status indicator (pulsing dot)
- "Due for review" hero card → start training
- Mastery overview: segmented bar + legend (Unseen / Learning / Reviewing / Mastered)
- Quick stats: word count / root count / text count (tap → list screens)
- "Jump back in": horizontal scroll of recent words

### Dictionary (Words)
- Word list: search + mastery filter chips + paginated rows
- Word detail: Arabic hero, mastery bar, examples, root link, dictionary links, sibling words (same root)
- Quick-add word: bottom sheet, Arabic + translation required (covers "heard a word on the bus")
- Mastery update via training result (not manual in this version)

### Roots
- Root list: Arabic letters large, transliteration, meaning, form count
- Root detail: hero with semantic note, derivation timeline with links to words

### Texts
- Text list: dialect/difficulty chips, Arabic title, "Read interlinear" label
- Text reader: interlinear / plain toggle, RTL token grid, token tap → bottom sheet, translation

### Training
- Start session → flashcard loop
- Card: Arabic front, reveal → transliteration + translation + example
- Swipe right = "Got it", swipe left = "Again" (also buttons)
- Session summary: accuracy %, correct/incorrect word list, done / train again

### Settings
- Base URL input + "Test connection" button
- Connection status written to app-wide state

---

## Out of scope (web app owns these)

- Creating or editing texts, sentences, tokens
- Creating or editing roots
- AI enrichment, auto-tokenize, AI insight
- Dictionary link management
- Print / export
- Analytics beyond the Home overview

---

## Tech stack

| Layer | Choice | Reason |
|---|---|---|
| Language | Kotlin 2.x | Matches backend, idiomatic Android |
| UI | Jetpack Compose | Modern, declarative, no XML layouts |
| Navigation | Navigation Compose 2.8+ (type-safe) | Compile-time safe routes via `@Serializable` destinations |
| HTTP | Ktor Client (Android engine) | Same library as backend; kotlinx.serialization already known |
| Serialization | kotlinx.serialization | Matches backend DTOs exactly |
| Async | Kotlin coroutines + Flow | Standard Compose async model |
| State | ViewModel + StateFlow | No extra lib; integrates with `collectAsStateWithLifecycle()` |
| Local cache | Room | SQLite, for offline fallback (Phase 8) |
| Preferences | DataStore (Preferences) | Base URL and other user prefs |
| DI | Hilt | Standard Android DI, KSP-backed, good Compose integration |
| Build | Gradle (Kotlin DSL) + Version Catalogs | Modern build setup |
| Min SDK | 29 (Android 10) | ~95% device coverage; modern APIs available |
| Target SDK | 35 | |

---

## Architecture

### Layer rules (same Clean/Onion approach as backend)

```
UI (Composables + ViewModels)
    ↓ calls
Repository interfaces (domain layer — no Android deps)
    ↓ implemented by
Remote (ApiClient) + Local (Room DAOs)
```

- ViewModels hold `StateFlow<UiState>` — one sealed UiState per screen
- Screens are stateless — they observe ViewModel state and dispatch events
- No business logic in Composables
- DTOs live in `data/api/dto/` — never leak into UI
- Domain models in `domain/model/` — pure Kotlin, no framework deps

### ViewModel pattern

```kotlin
// One sealed class per screen:
sealed interface WordListUiState {
    data object Loading : WordListUiState
    data class Success(val words: List<Word>, val hasMore: Boolean) : WordListUiState
    data class Error(val message: String) : WordListUiState
}
```

### Repository pattern

```kotlin
interface WordRepository {
    suspend fun getWords(query: String?, masteryFilter: String?, page: Int): Result<PagedResult<Word>>
    suspend fun getWord(id: String): Result<Word>
    suspend fun createWord(draft: WordDraft): Result<Word>
}
```

Remote implementation delegates to `ApiClient`. Local implementation (Phase 8) wraps Room DAOs.
Hilt provides the binding in `di/RepositoryModule.kt`.

---

## API contract

All calls to the Ktor backend. Base URL from DataStore. All responses are JSON.
Error envelope: `{ "error": "...", "code": "..." }`.
Pagination: `{ "items": [...], "total": N, "page": N, "size": N }`.

Key endpoints used:

| Screen | Endpoints |
|---|---|
| Home | `GET /api/v1/analytics/overview`, `GET /api/v1/words?size=6` |
| Word list | `GET /api/v1/words?q=&masteryLevel=&page=&size=` |
| Word detail | `GET /api/v1/words/{id}`, `GET /api/v1/words/{id}/examples` |
| Add word | `POST /api/v1/words` |
| Root list | `GET /api/v1/roots` |
| Root detail | `GET /api/v1/roots/{id}` |
| Text list | `GET /api/v1/texts` |
| Text reader | `GET /api/v1/texts/{id}`, `GET /api/v1/texts/{id}/sentences` |
| Training | `POST /api/v1/training/sessions`, `POST .../results`, `POST .../complete` |
| Settings test | `GET /api/v1/analytics/overview` (ping) |

DTOs: generate or manually mirror from `GET /api/v1/openapi.json`. Never hand-invent types.

---

## Network security

Plain HTTP to Tailscale subnet only. Configured via `res/xml/network_security_config.xml`
(see `android-plan.md` Phase 0.5). No TLS certificate setup needed for personal Tailscale use.

---

## Design

All visual decisions from the interactive prototype (`Qalam.dc.html`). We use **Material 3** as our 
design system engine, customized with our specific tokens. See `android-design.md`.

Key non-negotiables:
- Warm parchment palette mapped to M3 roles — not default Material 3 colors.
- Three font families: Hanken Grotesk (UI), Newsreader (prose/transliteration), Amiri (Arabic)
- Arabic text: always RTL, Amiri font, ≥ 24sp for body, larger for hero
- Mastery colors: Unseen `#A99F8B` · Learning `#B07D26` · Reviewing `#2F6E9E` · Mastered `#1F6F5C`
- Light mode only — no `.dark` variants ever

---

## Build & install

```bash
# Build
./gradlew assembleDebug

# Install via USB
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Install via wireless ADB (requires Developer Options → Wireless Debugging on phone)
adb pair <ip>:<pairing-port>
adb connect <ip>:<port>
adb install app/build/outputs/apk/debug/app-debug.apk
```

Phone setup: Settings → About phone → tap Build Number 7× to unlock Developer Options.
Enable: USB Debugging + Wireless Debugging (for ADB wireless).
No "Install unknown apps" permission needed — ADB install bypasses that.

---

## Phase summary

| Phase | Deliverable |
|---|---|
| 0 | Project builds, fonts loaded, network config set |
| 1 | Theme, navigation, bottom nav, empty screens |
| 2 | Settings screen, Ktor client, connection indicator |
| 3 | Word list, word detail, quick-add word |
| 4 | Root list, root detail |
| 5 | Text list, text reader with interlinear view |
| 6 | Training flashcard loop + summary screen |
| 7 | Home screen (depends on all API calls) |
| 8 | Offline handling, Room cache, build polish |
