# Qalam Android — Implementation Plan

Target reader: an AI agent or developer with no prior Android/Compose experience building 
this app. Follow phases in order. Each phase ships something runnable.

Companion documents (read before starting):
- `android-spec.md` — architecture, stack decisions, API contract
- `android-design.md` — colors, fonts, spacing, screen inventory, interactions

---

## Prerequisites

```bash
# Install Android Studio (includes SDK, emulator, adb)
# https://developer.android.com/studio
# After install, create one AVD: Pixel 7 / API 35

# Verify tools
adb version
./gradlew --version   # inside project root after creation
```

Minimum SDK: 29. Target SDK: 35. Language: Kotlin 2.x. Build system: Gradle (Kotlin DSL).

---

## Phase 0 — Project bootstrap

### ✅ 0.1 Create project

In Android Studio: **New Project → Empty Activity** (Compose template).
- Package name: `com.toni.qalam`
- Save location: new `qalam-android/` repo
- Language: Kotlin
- Min SDK: 29

### ✅ 0.2 Version catalog

`gradle/libs.versions.toml` exists with Compose BOM, basic Compose deps, and detekt.
Grows incrementally — new entries are added at the start of the phase that first needs them.

### ✅ 0.3 app/build.gradle.kts — minimal Compose skeleton

Current actual state — only what is wired and needed right now:

```kotlin
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.detekt)
}

android {
    namespace = "com.tonihacks.qalam"
    compileSdk = 36
    defaultConfig {
        applicationId = "com.tonihacks.qalam"
        minSdk = 31
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures { compose = true }
}

kotlin { jvmToolchain(17) }

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
}
```

### 🔄 0.4 Fonts — **current slice** (download + res/font/ + Type.kt)

Download and add to `app/src/main/res/font/`:
- `hanken_grotesk_regular.ttf`, `_medium.ttf`, `_semibold.ttf`, `_bold.ttf`
- `newsreader_regular.ttf`, `_italic.ttf`
- `amiri_regular.ttf`, `_bold.ttf`

Material Symbols: use `androidx.compose.material:material-icons-extended` (added in Phase 1.2
alongside navigation deps). For `account_tree` (roots) — verify it's in the extended set;
if not, use SVG vector drawable.

---

## Phase 1 — Theme + skeleton navigation

Goal: app launches, shows bottom nav, can switch between 4 empty screens. We use **Material 3** 
as the foundation, overriding its defaults with our design tokens.

### 🔄 1.1 Theme (`ui/theme/`) — Color.kt ✅ · Theme.kt ✅ · Type.kt see Phase 0.4

**Color.kt** — define every token from `android-design.md` as a named `Color`.

**Type.kt** — define `Typography` using the three font families. Map them to M3 roles
(Newsreader → display/headline; Hanken Grotesk → title/body/label; Amiri declared but not
in the Typography scale — applied explicitly via `ArabicText` composable).

**Theme.kt** — `QalamTheme` wraps content in `MaterialTheme` with a custom `ColorScheme`
mapped from our tokens to M3 roles. This ensures standard M3 components (Buttons, Chips,
NavigationBars) automatically adopt the Qalam visual identity. The app is light-only.

### ❌ 1.2 Navigation

**Build setup (do this first):** add to `libs.versions.toml` and wire into `build.gradle.kts`:
- Plugin: `kotlin-android` (`org.jetbrains.kotlin.android`) — needed to compile Kotlin for Android
- Plugin: `kotlin-serialization` (`org.jetbrains.kotlin.plugin.serialization`) — needed for type-safe nav
- Lib: `navigation-compose`
- Lib: `compose-icons-extended` (material-icons-extended, used by bottom nav + throughout app)
- Lib: `kotlinx-serialization-json` (required by navigation-compose type safety)

Use type-safe Navigation Compose (2.8+). Define a sealed hierarchy:

```kotlin
// navigation/Destination.kt
@Serializable object Home
@Serializable object WordList
@Serializable data class WordDetail(val wordId: String)
@Serializable object RootList
@Serializable data class RootDetail(val rootId: String)
@Serializable object TextList
@Serializable data class TextDetail(val textId: String)
@Serializable object Training
@Serializable object TrainingSummary  // carries result via ViewModel, not nav args
```

**MainNavHost.kt** — `NavHost` with one composable per destination.
`MainActivity.kt` — single activity, hosts the scaffold:

```kotlin
// Scaffold structure:
// - topBar: none (screens manage their own top area)
// - bottomBar: QalamBottomNav (hidden on Training / Summary)
// - content: NavHost
// - floatingActionButton: Train FAB (shown on Home + WordList)
```

### ❌ 1.3 Bottom nav component

`components/QalamBottomNav.kt` — 4 tabs, active/inactive states per design spec.
Active = filled icon + `primary-c` pill. Inactive = outlined icon only.

Deliverable: `./gradlew assembleDebug` succeeds, app runs, tabs navigate.

---

## Phase 2 — Settings + connection layer

Goal: user can enter a base URL, app verifies it, shows connection status.

**Build setup (do this first):** add to `libs.versions.toml` and wire into `build.gradle.kts`:
- Plugin: `hilt` (`com.google.dagger.hilt.android`) + `ksp` (`com.google.devtools.ksp`)
  — Hilt is Android's DI framework built on Dagger; KSP is the annotation processor that generates its code
- Lib: `hilt-android` + `hilt-android-compiler` (ksp) + `hilt-navigation-compose`
- Lib: `ktor-client-core`, `ktor-client-android`, `ktor-client-content-negotiation`,
  `ktor-serialization-kotlinx-json`, `ktor-client-logging`
- Lib: `datastore-preferences`
- Also add `@HiltAndroidApp` annotation to `QalamApp : Application` (new file) and register
  it in `AndroidManifest.xml` — Hilt requires an annotated Application class as its root.

**Network security config (deferred from Phase 0):**

`app/src/main/res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="true">ts.net</domain>
        <domain includeSubdomains="false">10.0.2.2</domain>
    </domain-config>
</network-security-config>
```

`AndroidManifest.xml` additions:
```xml
<uses-permission android:name="android.permission.INTERNET" />
<application
    android:name=".QalamApp"
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

### 2.1 DataStore

`data/local/PreferencesRepository.kt`:
```kotlin
val baseUrl: Flow<String>   // default: "http://100.x.x.x:8080"
suspend fun setBaseUrl(url: String)
```

### 2.2 Ktor client

`data/api/QalamHttpClient.kt`:
```kotlin
fun buildHttpClient(): HttpClient = HttpClient(Android) {
    install(ContentNegotiation) { json(Json { ignoreUnknownKeys = true }) }
    install(Logging) { level = LogLevel.HEADERS }
    // timeout: connect 5s, request 15s
}
```

Client receives `baseUrl` as a parameter at call time (not injected at construction),
because the user can change it in settings.

`data/api/ApiClient.kt` — wraps all endpoint calls, returns `Result<T>`.
All calls catch `IOException` and `HttpException` → `Result.failure(...)`.

### 2.3 Settings screen

`ui/settings/SettingsScreen.kt`:
- Text field: base URL input (pre-filled from DataStore)
- "Test connection" button → `GET /api/v1/analytics/overview` → show ✓ or error
- Save button writes to DataStore

### 2.4 Connection indicator on Home

Home screen top-right: pulsing green dot + "MacBook" label (or "Offline" + terra dot).
Driven by a `SettingsViewModel` that pings the API every time the app foregrounds.

Deliverable: can enter `http://100.x.x.x:8080`, test connection, save.

---

## Phase 3 — Words

### 3.1 DTOs

`data/api/dto/WordDto.kt` — mirror the backend response shape exactly.
Source of truth: `GET /api/v1/words` response schema in `documentation.yaml`.

```kotlin
@Serializable
data class WordDto(
    val id: String,
    val arabicText: String,
    val transliteration: String?,
    val translation: String?,
    val partOfSpeech: String,
    val dialect: String,
    val difficulty: String,
    val masteryLevel: String,
    val rootId: String?,
)

@Serializable
data class PagedResponse<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val size: Int,
)
```

### 3.2 Word list screen

`ui/words/WordListScreen.kt`:
- Title "Words" (Newsreader 34sp) + entry count (Newsreader italic)
- Search bar (surface card, search icon, clear button)
- Horizontal chip row: All · Unseen · Learning · Reviewing · Mastered
  - Active chip: mastery color background; inactive: surface
- Vertical list of word rows:
  - Left: mastery dot + label + POS, transliteration (italic), translation
  - Right: Arabic text (Amiri 34sp)
- Pagination: load more on scroll-to-bottom (`LazyColumn` + `LazyListState`)

ViewModel: `WordListViewModel` holds `StateFlow<WordListUiState>`.
UiState holds: query, activeFilter, items, isLoading, error, hasMore.

### 3.3 Word detail screen

`ui/words/WordDetailScreen.kt` (layout top-to-bottom):

1. **Top bar**: back arrow (left) + bookmark icon (right, stub for now)
2. **Hero**: Arabic (Amiri 74sp, centered) + transliteration (Newsreader italic 22sp) + translation (18sp bold)
3. **Badges**: POS chip + dialect chip (surface-2 background)
4. **Mastery card**: label + 4-segment progress bar (filled with mastery color up to level)
5. **Examples section** (uppercase label): cards with Arabic (26sp RTL), transliteration italic, English
6. **Root link card** (gold-c background): root Arabic + meaning → navigates to Root detail
7. **Dictionaries section**: list rows with external link icon (open in browser via `Intent`)
8. **Same root section**: horizontal scroll of sibling word chips → navigate to Word detail

Fetch: `GET /api/v1/words/{id}` + `GET /api/v1/words/{id}/examples` in parallel.

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
with `{ dialect: "MSA", size: 20 }` (configurable later).

The session response includes the word deck. Store in `TrainingViewModel`.

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
# USB install
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk

# Wireless (phone and laptop on same Tailscale network)
# On phone: Developer Options → Wireless debugging → pair
adb pair <ip>:<port>   # use pairing code shown on phone
adb connect <ip>:<port>
adb install app/build/outputs/apk/debug/app-debug.apk
```

Enable on phone: Settings → About phone → tap Build Number 7× → Developer Options →
USB Debugging + Wireless Debugging (for wireless). Install Unknown Apps not needed for ADB.

### 8.4 Build variants

`buildTypes` in `build.gradle.kts`:
- `debug`: default, debuggable, no proguard
- `release`: minified, no debuggable flag. Sign with a local keystore.

For personal sideloaded use, debug is sufficient indefinitely.

---

## File structure (final)

```
app/src/main/
├── AndroidManifest.xml
├── res/
│   ├── font/           (hanken_grotesk_*, newsreader_*, amiri_*)
│   └── xml/
│       └── network_security_config.xml
└── kotlin/com/toni/qalam/
    ├── MainActivity.kt
    ├── QalamApp.kt         (Hilt @HiltAndroidApp)
    ├── data/
    │   ├── api/
    │   │   ├── QalamHttpClient.kt
    │   │   ├── ApiClient.kt
    │   │   └── dto/        (one file per domain: WordDto, RootDto, TextDto, TrainingDto)
    │   ├── local/
    │   │   ├── PreferencesRepository.kt
    │   │   ├── QalamDatabase.kt    (Room, Phase 8)
    │   │   └── dao/
    │   └── repository/
    │       ├── WordRepository.kt
    │       ├── RootRepository.kt
    │       ├── TextRepository.kt
    │       └── TrainingRepository.kt
    ├── domain/
    │   └── model/          (Word, Root, Text, TrainingSession — pure Kotlin, no Android deps)
    ├── navigation/
    │   ├── Destination.kt
    │   └── MainNavHost.kt
    ├── di/
    │   ├── NetworkModule.kt
    │   └── RepositoryModule.kt
    └── ui/
        ├── theme/
        │   ├── Color.kt
        │   ├── Type.kt
        │   └── Theme.kt
        ├── components/
        │   ├── QalamBottomNav.kt
        │   ├── ArabicText.kt       (reusable composable enforcing RTL + Amiri font)
        │   ├── MasteryBar.kt
        │   ├── MasteryChip.kt
        │   ├── OfflineBanner.kt
        │   └── TokenBottomSheet.kt
        ├── home/
        │   ├── HomeScreen.kt
        │   └── HomeViewModel.kt
        ├── words/
        │   ├── WordListScreen.kt
        │   ├── WordListViewModel.kt
        │   ├── WordDetailScreen.kt
        │   ├── WordDetailViewModel.kt
        │   └── AddWordSheet.kt
        ├── roots/
        │   ├── RootListScreen.kt
        │   ├── RootListViewModel.kt
        │   ├── RootDetailScreen.kt
        │   └── RootDetailViewModel.kt
        ├── texts/
        │   ├── TextListScreen.kt
        │   ├── TextListViewModel.kt
        │   ├── TextReaderScreen.kt
        │   └── TextReaderViewModel.kt
        ├── training/
        │   ├── TrainingScreen.kt
        │   ├── TrainingSummaryScreen.kt
        │   └── TrainingViewModel.kt
        └── settings/
            ├── SettingsScreen.kt
            └── SettingsViewModel.kt
```

---

## Compose concepts to know before starting

These are non-obvious for someone coming from web/backend:

| Concept | What it is | Where used |
|---|---|---|
| `@Composable` | Function that emits UI, not returns it | Every screen and component |
| `remember` / `rememberSaveable` | Survive recomposition / also survive screen rotation | Local UI state |
| `StateFlow` → `collectAsStateWithLifecycle()` | ViewModel state → Compose state | All ViewModels |
| `LazyColumn` | Virtualized scrolling list | Word list, root list |
| `LazyRow` | Horizontal virtualized list | "Jump back in" row, filter chips |
| `FlowRow` | Wrapping row (like CSS flexbox wrap) | Token grid in text reader |
| `AnimatedVisibility` | Show/hide with animation | Bottom sheet, offline banner |
| `ModalBottomSheet` | Material 3 bottom sheet | Token sheet |
| `detectDragGestures` | Pointer input for swipe | Training card swipe |
| `animateFloatAsState` | Animate a single float | Card drag opacity badges |
| `NavBackStackEntry` | Retrieve ViewModel scoped to nav graph | TrainingViewModel (shared between Training + Summary) |
| Hilt `@HiltViewModel` | ViewModel with DI | All ViewModels |
| `hiltViewModel()` | Get ViewModel in Composable | All screens |
| `CompositionLocalProvider` | Pass implicit values down the tree | Base URL, theme overrides |
