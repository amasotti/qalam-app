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

## 🔄 Phase 3 — Words

### ✅ 3.1 DTOs

`data/api/dto/WordDto.kt` + `DictionaryLinkDto` + `ExampleDto`. Mapping via `.toDomain()` extension fns.

### ✅ 3.2 Word list screen

`ui/words/WordListScreen.kt`: search bar, mastery filter chips, paginated lazy list, `WordListViewModel`.

### ✅ 3.3 Word detail screen

`ui/words/WordDetailScreen.kt` — all sections implemented:
1. Top bar (back + bookmark stub)
2. Hero: Arabic `displayLarge` + transliteration + translation — wrapped in `SelectionContainer` (long-press copy works)
3. POS + dialect chips
4. Mastery card (`MasteryCard.kt`)
5. Examples (`ExampleCard.kt`) — wrapped in `SelectionContainer`
6. Root link card (gold-c, navigates to Root detail)
7. Dictionaries: pill chips (`DictionaryRow.kt`) — `VolumeUp` icon for Forvo/pronunciation sources, `MenuBook` for all others; opens browser via `Intent`
8. Notes — wrapped in `SelectionContainer`

### 3.4 Quick-add word

FAB long-press or dedicated "+" button on Word list top bar opens a bottom sheet:
- Required: Arabic text field (RTL input), translation
- Optional: transliteration, POS selector, dialect selector
- "Save" → `POST /api/v1/words` → close sheet, refresh list

Deliverable: can browse, search/filter words, view detail, add a word.

---

## Phase 4 — Roots

### 4.1 Root list screen

`ui/roots/RootListScreen.kt`:
- Title "Roots" + count + subtitle text ("The skeleton of the language…")
- Vertical list of root cards:
  - Arabic letters (Amiri 38sp, letter-spacing 6dp)
  - Transliteration (Newsreader italic, gold color) + meaning
  - Form count (22sp bold, right-aligned)

### 4.2 Root detail screen

`ui/roots/RootDetailScreen.kt`:
1. **Back button**
2. **Hero card** (gold gradient `#F2E2BD → #F8EED5`): large Arabic letters (62sp, letter-spacing 10dp), transliteration (gold italic), meaning
3. **Semantic note card** (surface): `auto_awesome` icon + "Semantic note" label + prose text (Newsreader 16.5sp)
4. **Derivation tree** (section label): vertical timeline with gold dots —
   each row: Arabic (30sp) + transliteration + gloss. Linked forms (have a `wordId`) are tappable → Word detail. Unlinked: 62% opacity, no chevron.

Fetch: `GET /api/v1/roots/{id}` (includes forms via the `words` relation).

Deliverable: can browse roots and their semantic families.

---

## Phase 5 — Texts

### 5.1 Text list screen

`ui/texts/TextListScreen.kt`:
- Title "Texts" + passage count
- Vertical list of text cards:
  - Dialect chip (lapis) + difficulty chip
  - Arabic title (Amiri 30sp, RTL)
  - English title (Newsreader italic)
  - "Read interlinear" link with `auto_stories` icon

### 5.2 Text reader screen

`ui/texts/TextReaderScreen.kt`:
1. **Top bar**: back + italic English title
2. **Arabic title** (32sp RTL) + dialect/difficulty/source chips
3. **Toggle**: Interlinear | Plain (segmented control)
4. **Interlinear view** (`FlowRow` with RTL direction):
   - Each token: Arabic (30sp) + transliteration (lapis italic 12.5sp) + gloss (12sp)
   - Linked tokens (have a `wordId`): lapis-c background
   - Tap → bottom sheet
5. **Plain view**: full Arabic text concatenated, 34sp, RTL
6. **Translation card**: "Translation" label + Newsreader prose
7. **Token bottom sheet**: large Arabic (52sp) + transliteration + gloss + "View full entry" button

Fetch: `GET /api/v1/texts/{id}` + `GET /api/v1/texts/{id}/sentences` (for tokens).

Note: `FlowRow` is in `Compose Foundation` — use `androidx.compose.foundation.layout.FlowRow`
(stable since Compose 1.7).

Deliverable: can read texts interlinearly, tap words, navigate to Word detail from text.

---

## Phase 6 — Training

### 6.1 Training setup

No explicit setup screen — matches the prototype.
"Start training session" on Home and the FAB both call `POST /api/v1/training/sessions`
with `{ mode: "ARABIC_TO_TRANSLATION", size: 20 }` (configurable later).

The session response includes the word deck (`words: List<TrainingSessionWordResponse>`).
Each word embeds up to 2 examples and synonym/antonym relations — no separate fetch needed.
Store deck in `TrainingViewModel`.

### 6.2 Flashcard screen

`ui/training/TrainingScreen.kt`:

**Layout** (no bottom nav, no FAB):
- Top: close (✕) + progress bar + "N / M" counter
- Center: card stack (next card peeking behind, scaled 0.93 + offset 20dp)
- Bottom: "Show answer" button (pre-reveal) or "Again" + "Got it" row (post-reveal)
- Hint: "Swipe right if you knew it · left to review again"

**Card state machine:**
- `notRevealed`: show Arabic only, "Tap to reveal" hint
- `revealed`: show Arabic + divider + transliteration + translation + POS + example

**Swipe gesture** (use `detectDragGestures` from Compose):
```
onDrag: update dragX offset
onDragEnd:
  if dragX > 90dp → grade correct (swipe right)
  if dragX < -90dp → grade incorrect (swipe left)
  else → spring back to 0
```
"KNEW IT ✓" badge: left side, opacity = `(dragX / 90).coerceIn(0f, 1f)`
"AGAIN ↻" badge: right side, opacity = `(-dragX / 90).coerceIn(0f, 1f)`
Card rotation: `dragX * 0.04f` degrees

After grading: `POST /api/v1/training/sessions/{id}/results` with word result.

### 6.3 Summary screen

`ui/training/SummaryScreen.kt`:
- Medal icon circle (primary-c background)
- "Session complete" (Newsreader 30sp) + accuracy %
- Two stat cards: "Knew it" (primary-c) + "To review" (terra-c) with counts
- Scrollable word result list (check_circle green / replay_circle_filled terra)
- "Done" → Home, "Train again" → new session

Call `POST /api/v1/training/sessions/{id}/complete` when summary is reached.

Deliverable: full training loop, session recorded in backend DB.

---

## Phase 7 — Home screen

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
