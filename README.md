# Qalam Android

Native Android companion to [Qalam](https://github.com/amasotti/qalam) — a personal Arabic vocabulary and text study tool.
Read-heavy, training-focused. Connects to the Ktor backend over Tailscale. No Play Store. Sideloaded only.

## What it does

| Screen | Purpose |
|--------|---------|
| Home | Mastery overview, due-for-review count, recent words |
| Words | Dictionary with search, mastery filter, quick-add |
| Roots | Trilateral root browser with derivation graph |
| Texts | Interlinear reader with per-token gloss |
| Training | SRS flashcard loop (swipe right = got it, left = again) |
| Settings | Backend URL + connection test |

## Prerequisites

- Android Studio Meerkat or later
- JDK 17+
- Android SDK (min API 31, target API 36)
- [ADB](https://developer.android.com/tools/adb) on PATH
- Phone with Developer Options → USB Debugging enabled
- [Tailscale](https://tailscale.com) on both phone and laptop (backend reachable at `http://<laptop>.ts.net:8080`)

## Build & install

```bash
# Build debug APK
just build

# Install on connected device (USB)
just install

# Build + install + launch in one step
just run

# Wireless ADB (pair first via Developer Options → Wireless Debugging)
just pair IP=192.168.x.x PORT=xxxxx
just connect IP=192.168.x.x PORT=xxxxx
just install
```

## First launch

Open Settings, set the backend URL to `http://<your-laptop-hostname>.ts.net:8080`, tap **Test connection**.
The pulsing dot on the Home screen turns green when the backend is reachable.

## Tech stack

| | |
|---|---|
| Language | Kotlin 2.x |
| UI | Jetpack Compose |
| Navigation | Navigation Compose (type-safe `@Serializable` routes) |
| HTTP | Ktor Client (Android engine) |
| Serialization | kotlinx.serialization |
| State | ViewModel + StateFlow |
| Local cache | Room (offline fallback) |
| Preferences | DataStore |
| DI | Hilt |
| Build | Gradle Kotlin DSL + Version Catalogs |

## Docs

- [`docs/android-app.md`](docs/android-app.md) — full spec, API contract, architecture rules
- [`docs/android-design.md`](docs/android-design.md) — color tokens, typography, spacing, animations
- [`docs/android-plan.md`](docs/android-plan.md) — phased implementation guide (8 phases)
- [`wireframe/Qalam.dc.html`](wireframe/Qalam.dc.html) — interactive prototype (visual source of truth)
