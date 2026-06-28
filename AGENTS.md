# Project Steering: qalam-app

This file guides AI agents collaborating on the `qalam-app` project. It complements the global guidelines in `~/.claude/CLAUDE.md`.

## Project Context
- **Name:** qalam-app
- **Purpose:** Android application (details to be discovered as we build).
- **Structure:** Single sub-project `:app` (Android App).

## Tech Stack
- **Language:** Kotlin (JVM 17 target, 2.4.0 plugin).
- **UI Framework:** Jetpack Compose (Compose BOM 2026.02.01).
- **Build System:** Gradle with Kotlin DSL and Version Catalogs (`libs.versions.toml`).
- **Minimum SDK:** 31 (Android 12).
- **Target SDK:** 36 (Android 15 / Vanilla Ice Cream).

## Quality Gates & Tooling
- **Linting:** `detekt` is mandatory for Kotlin code. Run it before suggesting completion of a slice.
- **Testing:** JUnit 4 for unit tests, Espresso and Compose UI Test for instrumentation.
- **Task Runner:** `just` (via `justfile`).

## Collaboration Mode: Pair Programming
We operate primarily in **Pair Programming** mode as defined in the `pair-programming` skill.
- **Default Mode:** Coach mode (User writes, Agent guides).
- **Slicing:** Work in small, reviewable slices.
- **Guidance:** Create ephemeral guidance files in `.pairing/<YYYY-MM-DD>-<task-slug>.md` for implementation tasks.
- **Teaching:** Explain genuinely new or domain-specific concepts (2-4 sentences). Skip the trivial.

## Knowledge Base & Conventions
- **Clean Architecture:** Prefer separation of concerns (UI, Domain, Data) as the project grows.
- **Modern Android:** Follow Material 3 guidelines and Compose best practices.
- **Version Control:** Human-commits rule (Agent never commits).

## Active Task
- Initial setup and steering definition.
- Next: Explore project requirements and define the first feature slice.
