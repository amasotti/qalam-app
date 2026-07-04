# Project Steering: Qalam Android

Source of truth for AI agents (Claude, Gemini, …) working on `qalamapp`. Defines architecture,
collaboration mode, and the hard rules. The MVP is complete; this file governs ongoing development.

## Fast-start

Read in this order, nothing else until needed:

1. **This file** — rules, architecture, workflow.
2. **`docs/architecture.md`** — layer rules, backend API contract, key decisions.
3. **`docs/design-system.md`** — tokens, only when touching UI.

Read source files on demand when editing or verifying — never speculatively. Before implementing any
DTO or API call, fetch the actual OpenAPI spec
(`http://localhost:8085/api/v1/swagger-ui/documentation.yaml`). Never infer field names, paths, or
response shapes — guessing is a critical error.

## Project context

- **Purpose:** personal Arabic learning app — master vocabulary and grammar through a structured,
  high-quality, visual UI. Single user (Toni).
- **Environment:** connects to a Ktor backend over Tailscale. No auth (perimeter security). No Play
  Store — sideloaded debug/release APKs.
- **Backend contract:** `docs/architecture.md`; OpenAPI spec is authoritative.

## Collaboration mode: pair programming

Operate in **Pair Programming** mode (see the `pair-programming` skill).

- **Default: Coach mode** — the user writes code, the agent guides one small reviewable slice at a
  time. Only "Take Over" a slice when explicitly asked.
- **Teaching calibration:** the user is an experienced Kotlin backend dev (Ktor/server-side) with
  **zero prior Android experience**. Explain Android-specific concepts in depth the first time they
  matter: the Compose runtime (`@Composable`, recomposition, slots), the resource system
  (`res/font/`, `res/xml/`, naming rules), Activity lifecycle, Hilt/KSP annotation processing,
  Gradle/AGP conventions, `AndroidManifest.xml`. Skip trivial Kotlin syntax.
- **Proactive mentorship:** challenge decisions that deviate from idiomatic Android / senior best
  practice; propose the better alternative before proceeding.
- **Ephemeral guidance:** for a non-trivial task, write a walkthrough in
  `.pairing/<YYYY-MM-DD>-<task-slug>.md`. These are working notes — never referenced from code,
  README, or committed docs, and never deleted (kept as history).
- **Human-commits rule:** agents never commit, amend, or push. All git actions are the user's.

## Tech stack & quality gates

- **Language:** Kotlin 2.4.0 (JVM 17). **AGP** 9.2.1.
- **UI:** Jetpack Compose + Material 3.
- **Navigation:** Navigation3 (`androidx.navigation3`) — not `navigation-compose`, no `@Serializable`
  routes. See `docs/architecture.md`.
- **DI:** Hilt (KSP). **HTTP:** Ktor Client 3.x. **Prefs:** DataStore.
- **SDK:** min 31, target/compile 37.
- **Build system:** Gradle Kotlin DSL + version catalog (`gradle/libs.versions.toml`).
- **Task runner:** `just` — `just build`, `just run`, `just detekt`, `just check`. See `justfile`.
- **Static analysis:** detekt (`config/detekt/detekt.yml`) is mandatory — run `just detekt` before
  finishing a slice.
- **Testing:** JUnit 4 (unit), Compose UI Test (instrumentation).

## Architecture — non-negotiable

```
UI (Composables + ViewModels)
      ↓
Repository interfaces  ← domain layer, zero Android dependencies
      ↓
Remote (ApiClient)     (+ Local/Room — not yet implemented)
```

- **ViewModels** expose `StateFlow<SealedUiState>` (`Loading / Success / Error`). Private
  `_x: MutableStateFlow`, public `x: StateFlow` — never expose mutable state.
- **Composables** are stateless: observe state, dispatch events, no business logic.
- **DTOs** in `data/api/dto/`, mapped via `toDomain()`; never leak into UI/domain.
- **Domain models** in `domain/model/` — pure Kotlin, no framework imports.
- **DI** is Hilt-only; bind implementations in `di/RepositoryModule.kt`.
- **Base URL** from DataStore, passed per-call to `ApiClient` — never hardcoded, never injected into
  the client.

Full rationale and the backend API contract: `docs/architecture.md`.

## UI — design & Arabic rules

- **Engine:** Material 3, skinned with Qalam tokens. Use standard M3 components (Scaffold, TopAppBar,
  Chips, Buttons).
- **MANDATORY — Qalam tokens only:** use `Typography.*` and named color constants (`QalamPrimary`,
  `QalamTerra`, `QalamInk`, `QalamBg`, …). Never `MaterialTheme.typography.*` or
  `MaterialTheme.colorScheme.*` directly — M3 defaults bypass the custom fonts and palette.
- **Fonts:** Hanken Grotesk (UI), Newsreader (prose), **Amiri (Arabic)**.
- **Arabic:** always `LayoutDirection.Rtl`, Amiri, `fontSize >= 24.sp`.
- **Mastery colors:** Unseen `#A99F8B` · Learning `#B07D26` · Reviewing `#2F6E9E` · Mastered `#1F6F5C`.
- **Light mode only** — no dark theme.

Full token set and interaction patterns: `docs/design-system.md`.

## Workflow on every slice

1. **Verify** — read the user's changed files before declaring anything correct.
2. **Run `just detekt`** (and `just test` when logic changed) before finishing.
3. **Update the `.pairing/` walkthrough** for the next step if the task continues.

Never fabricate success — run the check and report the real output.
