APP_ID := "com.tonihacks.qalam"
APK    := "app/build/outputs/apk/debug/app-debug.apk"

# List available recipes
default:
    @just --list

# ── Build ────────────────────────────────────────────────────────────────────

# Build debug APK
build:
    ./gradlew assembleDebug

# Build release APK
build-release:
    ./gradlew assembleRelease

# Clean build outputs
clean:
    ./gradlew clean

# ── Install & run ────────────────────────────────────────────────────────────

# Install debug APK on connected device (USB or already-connected wireless)
install: build
    adb install -r {{ APK }}

# Uninstall from device
uninstall:
    adb uninstall {{ APP_ID }}

# Build, install, and launch
run: install
    adb shell am start -n {{ APP_ID }}/.MainActivity

# Force-stop the app
stop:
    adb shell am force-stop {{ APP_ID }}

# ── Wireless ADB ─────────────────────────────────────────────────────────────

# Pair device via wireless debugging (get IP:port from Developer Options → Wireless Debugging → Pair device)
pair IP PORT:
    adb pair {{ IP }}:{{ PORT }}

# Connect to already-paired device (use the main wireless debugging IP:port, not the pairing port)
connect IP PORT:
    adb connect {{ IP }}:{{ PORT }}

# ── Debug ────────────────────────────────────────────────────────────────────

# Show logcat filtered to this app (Ctrl+C to stop)
logs:
    adb logcat --pid=$(adb shell pidof -s {{ APP_ID }}) 2>/dev/null || \
    adb logcat -s "{{ APP_ID }}"

# Show logcat filtered by tag (default: all app output)
logcat TAG="AndroidRuntime":
    adb logcat -s {{ TAG }}

# List connected ADB devices
devices:
    adb devices -l

# Clear app data (resets DataStore, forces Settings re-entry)
clear-data:
    adb shell pm clear {{ APP_ID }}

# ── Quality ──────────────────────────────────────────────────────────────────

# Run Android lint
lint:
    ./gradlew lint

# Run detekt static analysis
detekt:
    ./gradlew detekt

# Run unit tests
test:
    ./gradlew test

# Run instrumented tests (device required)
test-instrumented:
    ./gradlew connectedAndroidTest

# Run all quality checks (lint + detekt + unit tests)
check:
    ./gradlew lint detekt test

# ── Gradle ───────────────────────────────────────────────────────────────────

# Show dependency tree for the app module
deps:
    ./gradlew :app:dependencies

# Check for dependency version updates
deps-update:
    ./gradlew dependencyUpdates

# Sync Gradle (useful after editing libs.versions.toml)
sync:
    ./gradlew --refresh-dependencies :app:dependencies > /dev/null
