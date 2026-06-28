# Project Steering: Qalam Android

This file acts as the primary source of truth for AI agents (Claude, Gemini, etc.) collaborating on the `qalamapp` project. It defines the architecture, design tokens, and collaboration protocols.

## Project Context
- **Purpose:** Personal Arabic learning app (Qalam).
- **Core Goal:** Master Arabic vocabulary and grammar through a structured, visual, and high-quality UI.
- **Environment:** Connects to a Ktor backend via Tailscale. No auth required (perimeter security).
- **Primary References:**
    - Full Spec: `docs/android-app.md`
    - Design Tokens: `docs/android-design.md`
    - Build Plan: `docs/android-plan.md`
    - Visual Source of Truth: `wireframe/Qalam.dc.html`

## Collaboration Mode: Pair Programming
We operate in **Pair Programming** mode as defined in the `pair-programming` skill. 
- **Default Mode:** **Coach mode** (User writes, Agent guides). The Agent should only "Take Over" a slice if explicitly requested.
- **Slicing:** Every task must be broken into small, reviewable slices that advance the product.
- **Ephemeral Guidance:** For any implementation task, create a walkthrough in `.pairing/<YYYY-MM-DD>-<task-slug>.md`. This file should be technical, concise, and deleted upon task completion.
- **Teaching calibration:** User is an experienced Kotlin developer (server-side / Ktor / backend). Has **zero prior Android experience**. Explain Android-specific concepts at length in guidance files: the Compose runtime model (`@Composable`, recomposition, slots), Android resource system (`res/font/`, `res/xml/`, naming rules), Activity lifecycle, Hilt/KSP annotation processing, Gradle/AGP conventions, `AndroidManifest.xml` attributes. Skip trivial Kotlin syntax — the user knows it.
- **Proactive Mentorship:** Since the user is an Android novice, the Agent MUST challenge decisions that deviate from idiomatic Android standards or "senior developer" best practices, suggesting the superior alternative before proceeding.
- **Human-Commits Rule:** Agents never commit to git. All version control actions are performed by the user.

## Tech Stack & Quality Gates
- **Language:** Kotlin 2.4.0 (JVM 17 toolchain).
- **UI:** Jetpack Compose (BOM 2026.02.01) with Material 3.
- **Target SDK:** 36 (Android 15) / **Min SDK:** 31.
- **Build System:** Gradle (Kotlin DSL) + Version Catalogs.
- **Task Runner:** `just` (see `justfile` for `lint`, `detekt`, `check`, `build`).
- **Static Analysis:** `detekt` (v2.0.0-alpha.5) is mandatory. Run `just detekt` before finishing a slice.
- **Testing:** JUnit 4 (Unit), Compose UI Test (Instrumentation).

## Architecture — Non-Negotiable Layer Rules
```
UI (Composables + ViewModels)
    ↓
Repository interfaces  ← Domain layer, zero Android dependencies
    ↓
Remote (ApiClient) + Local (Room DAOs)
```
- **ViewModels:** Expose `StateFlow<SealedUiState>` (e.g., `Loading / Success / Error`).
- **Composables:** Must be stateless. Observe state, dispatch events, no business logic.
- **DTOs:** Live in `data/api/dto/`. Never leak into UI or domain layers.
- **Domain Models:** Live in `domain/model/`. Pure Kotlin, no framework imports.
- **DI:** Hilt is mandatory. Bind implementations in `di/RepositoryModule.kt`.
- **API:** Base URL from DataStore (never hardcoded). Derive DTOs from `GET /api/v1/openapi.json`.

## UI — Design & Arabic Rules
- **Design Engine:** Material 3 (M3). Use standard M3 components (Scaffold, TopAppBar, Chips, Buttons) but skin them with our tokens.
- **Palette:** Warm parchment (see `docs/android-design.md`). Map CSS variables to M3 ColorRoles.
- **Fonts:** Hanken Grotesk (UI), Newsreader (Prose), **Amiri (Arabic)**.
- **Arabic Handling:** Always use `layoutDirection = LayoutDirection.Rtl`, Amiri font, `fontSize >= 24.sp`.
- **Mastery Colors:** 
    - Unseen: `#A99F8B` | Learning: `#B07D26` | Reviewing: `#2F6E9E` | Mastered: `#1F6F5C`
- **Light Mode Only:** No dark theme support.
- **MANDATORY — Qalam tokens only:** All code examples and guidance files MUST use Qalam design tokens (see wireframe folder). Never use `MaterialTheme.typography.*` or `MaterialTheme.colorScheme.*` directly. Use `Typography.*` (from `com.tonihacks.qalam.ui.theme`) and named color constants (`QalamPrimary`, `QalamTerra`, `QalamInk`, `QalamBg`, etc.). M3 defaults bypass the custom fonts and palette entirely.

## Phase Discipline
We follow the 8 phases in `docs/android-plan.md`. Advancement is tracked *only* in that file. Never reference the plan or phase progress in code comments or READMEs.

## MANDATORY Slice Workflow
The Agent MUST follow these steps in order when completing a slice and moving to the next:

1. **Verify** the user's implementation (read the changed files).
2. **Update `docs/android-plan.md`** — mark the completed slice ✅, the current one 🔄, using the exact section numbering from the plan.
3. **Then** create the next pairing file.

Skipping step 2 is not permitted. The plan is the single progress source of truth for both the user and future agents.

