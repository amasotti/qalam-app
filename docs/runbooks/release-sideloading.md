# Runbook: Release build & sideloading

How to build a signed **release** APK of Qalam and install it on the phone without the Play Store.
For day-to-day development a **debug** build (`just install` / `just run`) is enough — use release when
you want the optimized, self-updatable build.

Release vs debug:

- **Optimized** — R8 shrinks/obfuscates/inlines; smaller, faster APK.
- **Self-updatable** — a release signed with the same key installs over itself without uninstalling
  (debug and release keys differ, so you cannot install one over the other).

## 1. Create a signing key (once)

Android requires every app to be signed; for personal use, self-sign:

```bash
keytool -genkey -v -keystore qalam-release.keystore -alias qalam-key \
  -keyalg RSA -keysize 2048 -validity 10000
```

Keep `qalam-release.keystore` safe — losing it means you must uninstall/reinstall to update the app.
Move it into `app/`.

## 2. Configure Gradle signing

Secrets live in git-ignored `local.properties` (not `gradle.properties`):

```properties
RELEASE_STORE_PASSWORD=your_password
RELEASE_KEY_ALIAS=qalam-key
RELEASE_KEY_PASSWORD=your_password
```

`project.findProperty()` only reads `gradle.properties`, so `app/build.gradle.kts` loads
`local.properties` explicitly:

```kotlin
import java.util.Properties

val localProps = Properties().apply {
    rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
}
```

The `signingConfigs["release"]` block then reads `localProps[...]` for store/key passwords and alias,
and `buildTypes.release` enables `isMinifyEnabled` + `proguardFiles(...)` and applies the signing
config. This is already wired in `app/build.gradle.kts`.

## 3. R8 / ProGuard rules

With `isMinifyEnabled = true`, **R8** (modern ProGuard) shrinks, obfuscates, and optimizes bytecode
before packaging. It is aggressive and cannot tell reflection-driven code from dead code, so libraries
that rely on reflection (Hilt, Ktor, kotlinx.serialization) break at runtime unless kept. `app/proguard-rules.pro`
tells R8 what not to touch:

```
# Keep line numbers for crash reporting
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keep,includedescriptorclasses class com.tonihacks.qalam.**$$serializer { *; }
-keepclassmembers class com.tonihacks.qalam.** { *** Companion; }
-keepclasseswithmembers class com.tonihacks.qalam.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-dontwarn dagger.hilt.**

# Ktor
-keep class io.ktor.** { *; }
-keep class kotlinx.coroutines.** { *; }
-dontwarn io.ktor.**
-dontwarn okhttp3.**
-dontwarn okio.**

# DataStore
-keep class androidx.datastore.** { *; }

# Data/domain models (serialized over the wire)
-keep class com.tonihacks.qalam.data.** { *; }
-keep class com.tonihacks.qalam.domain.** { *; }
```

If a release build crashes with `ClassNotFoundException` / `NoSuchMethodException` that debug does not:
find the class in the stack trace, add `-keep class fully.qualified.ClassName { *; }`, rebuild. Add
`-printusage build/outputs/usage.txt` to see everything R8 removed.

## 4. Build & install

```bash
just build-release        # ./gradlew assembleRelease → app/build/outputs/apk/release/app-release.apk
adb install -r app/build/outputs/apk/release/app-release.apk   # -r = replace, keeps data
```

Manual (no computer): send `app-release.apk` to the phone (Telegram/Drive/`adb push`), open it in a
file manager, allow **Install unknown apps** if prompted.

## Troubleshooting

- **"App not installed / package appears invalid"** — installing a release over a debug build (keys
  differ). Uninstall Qalam first, then `adb install`.
- **`SigningConfig "release" is missing required property "storePassword"`** — signing props are in
  `local.properties` but `build.gradle.kts` isn't loading it explicitly (see step 2).
- **"Supplied proguard configuration does not exist"** — `app/proguard-rules.pro` is missing (see
  step 3).
- **"Cleartext traffic not permitted"** — ensure `res/xml/network_security_config.xml` applies to the
  release build and the backend's Tailscale address is reachable from the phone.
