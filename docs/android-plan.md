# Qalam Android — Implementation Plan

> **Agent fast-start:** Read `AGENTS.md` → this file (current phase section only) →
> `docs/android-app.md` (API contract table). Completed phases are decision records —
> do not read them in full. Read source files on demand, not speculatively.

---

## ✅ Phase 0 — Project bootstrap
Package `com.tonihacks.qalam`, minSdk 31, targetSdk 37, Kotlin 2.4.0, JVM 17.
Compose BOM `2026.06.00`. Gradle Kotlin DSL. Version catalog: `gradle/libs.versions.toml`.
Fonts in `res/font/`: HankenGrotesk (full weight range), Newsreader, Amiri.
Task runner: `just build` = `./gradlew assembleDebug`. Full recipes in `justfile`.

---

## ✅ Phase 1 — Theme + navigation
`QalamTheme` wraps `MaterialTheme` with Qalam color tokens + custom `Typography`. Light-only.
**Navigation3** (`androidx.navigation3:1.1.3`) — NOT `navigation-compose` (maintenance mode).
`Destination` sealed interface in `navigation/Destination.kt`; entries are `data object`/`data class`, no `@Serializable`.
`backStack: SnapshotStateList<Any>` owned by `MainActivity`. Screens receive typed nav lambdas — never touch backStack directly.
`QalamBottomNav`: 4 tabs (Home, Words, Roots, Texts), filled/outlined icon toggle, `QalamPrimaryC` indicator pill.
`enableEdgeToEdge(statusBarStyle = SystemBarStyle.light(...))` required — without it, system icons are invisible on parchment background.

---

## ✅ Phase 2 — Settings + connection layer
**DI:** Hilt 2.60 + KSP 2.3.9. `QalamApp : Application` annotated `@HiltAndroidApp`. `MainActivity` annotated `@AndroidEntryPoint` — required for `hiltViewModel()` to work in any hosted composable.
**Network:** `<base-config cleartextTrafficPermitted="true"/>` — domain-config insufficient for raw Tailscale IPs; Tailscale is the security perimeter.
**DataStore:** `PreferencesRepository` — `val baseUrl: Flow<String>`, `suspend fun setBaseUrl(String)`. Default URL in `DEFAULT_URL` companion const.
**HTTP:** `ApiClient` `@Singleton @Inject constructor(HttpClient)`. Client built with `HttpClient(Android)`, `expectSuccess=true`, `ignoreUnknownKeys=true`. Connection test hits `GET /health` (not analytics endpoint). `baseUrl` passed at call time, not injected into client.
**Settings:** `SettingsViewModel` (`@HiltViewModel`) — `urlDraft: StateFlow<String>` seeded once from DataStore in `init`, `connectionStatus: StateFlow<ConnectionStatus>`, `testConnection()` + `saveUrl()`.
**Home indicator:** `HomeViewModel` pings `/health` via `repeatOnLifecycle(Lifecycle.State.STARTED)`. Pulsing `QalamPrimary` dot (online) or steady `QalamTerra` dot (offline). Hostname via `String.toUri().host`. `Settings` destination accessible from Home gear icon; back via `backStack.removeLastOrNull()` lambda.

---

## ✅ Phase 3 — Words
Implemented the words vertical end to end: API DTOs and domain mapping, repository methods, list/search/filter with pagination, detail view with examples/dictionary links/root navigation, and quick-add via `POST /api/v1/words`. Quick-add uses an M3 bottom sheet with RTL Amiri Arabic input, required Arabic + translation fields per product plan, optional transliteration, POS/dialect selectors backed by backend enum values, save loading state, and visible failure handling that keeps the sheet open. OpenAPI was checked for word endpoints; `CreateWordRequest` only requires `arabicText`, but the Android UI intentionally keeps translation required for the learner workflow.

---

## ✅ Phase 4 — Roots

Implemented the roots vertical end to end: OpenAPI-backed root DTOs and domain mapping, repository methods, Hilt binding, paginated root list, form counts derived from `GET /api/v1/words?rootId=...`, root detail, semantic note rendering from `analysis`, and navigation from derivation rows to word detail. The original plan assumed `GET /api/v1/roots/{id}` embedded forms, but the backend only returns root metadata; Android now combines `GET /api/v1/roots/{id}` with a root-filtered word list to build the semantic family view.

---

## ✅ Phase 5 — Texts

Implemented the texts vertical end to end: text list pagination, dialect/difficulty chips, Arabic preview with Amiri RTL rendering, reader detail, interlinear/plain toggle, `FlowRow` token layout, token bottom sheet, and navigation from linked tokens to word detail. The reader uses `GET /api/v1/texts/{id}` plus `GET /api/v1/texts/{id}/sentences`; an empty sentence list is valid for newly-created backend texts, but failed sentence fetches should be surfaced as errors rather than silently displayed as empty interlinear content. The earlier source/comments chip idea was dropped because the backend source field does not exist and comments are not useful for the current learner workflow.

---

## ✅ Phase 6 — Training

Implemented the full training loop against the existing backend training API. Home opens a compact
training setup screen where mode (`MIXED`, `NEW`, `LEARNING`, `KNOWN`) and size (`10`, `20`, `30`,
`50`) are selected before `POST /api/v1/training/sessions`. The flashcard screen uses the session
deck returned by the backend, including `frontSide`, examples, and relations; tapping reveals, swiping
after reveal records results, and the card follows the finger with a quick committed exit animation.
The inline completion panel intentionally stays minimal: it completes the session in the backend,
shows accuracy/counts, and offers Done or Train again.

---

## 🔄 Phase 7 — Home screen

`ui/home/HomeScreen.kt` (final screen, depends on all others):

**Top section**:
- Greeting (Newsreader italic, time-of-day: Good morning/afternoon/evening)
- "قلم" Arabic title (Amiri 40sp)
- "Qalam · Arabic" subtitle (uppercase, letter-spacing 3dp)
- Connection indicator: pulsing dot + "MacBook" / "Offline" label

**Due-for-review hero card** (green gradient, tappable → start training):
- "DUE FOR REVIEW" label (uppercase small)
- Large count + "words waiting" (Newsreader italic)
- "Start training session" pill button with bolt icon

**Mastery overview card**:
- Segmented bar (4 segments proportional to counts, 12dp height)
- Legend: dot + label + count for each level

**Quick stats grid** (3 columns):
- Words count → Word list
- Roots count → Root list
- Texts count → Text list

**"Jump back in" row** (horizontal scroll):
- Up to 6 recent words as small cards (mastery dot + Arabic + transliteration + gloss)

Fetch: `GET /api/v1/analytics/overview` for totals + mastery distribution.
Fetch: `GET /api/v1/words?size=6&sort=updatedAt` for recent words.

Deliverable: full app complete.

---

## Phase 8 — Polish & install

### 8.1 Offline handling

Every screen: if `ApiClient` throws, show offline banner (terra-c) + cached Room data if available.
`ConnectivityObserver` via `ConnectivityManager.NetworkCallback` → `StateFlow<Boolean>`.

### 8.2 Room cache (optional, implement if offline is painful without it)

**Build setup:** add to `libs.versions.toml`: `room-runtime`, `room-ktx`, `room-compiler`.
Wire `ksp(libs.room.compiler)` in `build.gradle.kts` — KSP plugin already present from Phase 2.

Cache words and roots locally. Sync on foreground. Room schema mirrors DTO shapes.
Use `@Database`, `@Entity`, `@Dao` — Room annotation processor (KSP).

### 8.3 Build & install

```bash
just build    # assembleDebug
just install  # build + adb install
just run      # build + install + launch
just pair IP PORT   # wireless ADB pairing
just connect IP PORT
```

### 8.4 Build variants

`debug`: default, debuggable. `release`: minified, signed. For personal sideloaded use, debug is sufficient.
