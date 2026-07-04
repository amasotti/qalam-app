# Runbook: Release & Sideloading

This guide explains how to build a production-ready (Release) version of Qalam and install it on your Android phone without using the Google Play Store.

## Why Release Mode?
*   **Performance:** Code is optimized (R8/ProGuard) and debug overhead is removed.
*   **Size:** The APK is significantly smaller.
*   **Persistence:** A signed release build can be updated over itself (if you use the same key), whereas debug builds often conflict with each other if the debug key changes.

---

## Step 1: Create a Signing Key (Keystore)
Android requires all apps to be signed. For personal use, you create your own "self-signed" certificate.

Run this in your terminal from the project root:
```bash
keytool -genkey -v -keystore qalam-release.keystore -alias qalam-key -keyalg RSA -keysize 2048 -validity 10000
```
*   **Password:** Pick something you'll remember (e.g., `qalam123`).
*   **Details:** You can leave the name/org fields blank or just put "Toni".
*   **Security:** Keep this file (`qalam-release.keystore`) safe. If you lose it, you'll have to uninstall and reinstall the app to update it.

---

## Step 2: Configure Gradle (Securely)
We want to sign the app without hardcoding passwords in `build.gradle.kts`.

1.  Move your `qalam-release.keystore` to the `app/` directory.
2.  Add the secrets to `/Users/antoniomasotti/toni/100_programming/qalamapp/local.properties` (which is git-ignored):
    ```properties
    RELEASE_STORE_PASSWORD=your_password
    RELEASE_KEY_ALIAS=qalam-key
    RELEASE_KEY_PASSWORD=your_password
    ```

3.  Update `app/build.gradle.kts`. **Important:** `project.findProperty()` reads `gradle.properties`, not `local.properties`. Load `local.properties` explicitly at the top of the file:
    ```kotlin
    import java.util.Properties

    val localProps = Properties().apply {
        rootProject.file("local.properties").takeIf { it.exists() }?.inputStream()?.use { load(it) }
    }
    ```
    Then use it in the signing config:
    ```kotlin
    android {
        // ...
        signingConfigs {
            create("release") {
                storeFile = file("qalam-release.keystore")
                storePassword = localProps["RELEASE_STORE_PASSWORD"] as String?
                keyAlias = localProps["RELEASE_KEY_ALIAS"] as String?
                keyPassword = localProps["RELEASE_KEY_PASSWORD"] as String?
            }
        }

        buildTypes {
            release {
                isMinifyEnabled = true // Enables code shrinking/obfuscation
                proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }
    ```

---

## Step 2b: Create `proguard-rules.pro`

### What is ProGuard / R8?

When `isMinifyEnabled = true` in your build config, the Android build tool **R8** runs on your bytecode before packaging. R8 is the modern replacement for ProGuard (same rule language, better optimizer). It does three things:

| What | Effect |
|------|--------|
| **Shrinking** | Removes unused classes, methods, fields. Smaller APK. |
| **Obfuscation** | Renames `MyViewModel` → `a`, `fetchWords` → `b`. Harder to reverse-engineer. |
| **Optimization** | Inlines small methods, removes dead branches. Faster runtime. |

The problem: R8 is aggressive. It cannot always tell the difference between "code called via reflection at runtime" and "dead code." Libraries that use reflection heavily (Hilt, Ktor, kotlinx.serialization) will break silently at runtime if R8 strips the wrong class.

**`proguard-rules.pro`** is your override file: you tell R8 what it must never touch.

### Why the build failed

`build.gradle.kts` references `"proguard-rules.pro"` in the `release` block but the file did not exist on disk. R8 refuses to proceed with a missing config file.

### Create the file

Create `app/proguard-rules.pro`:

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
-keepclassmembers class com.tonihacks.qalam.** {
    *** Companion;
}
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

# Your data/domain models (serialized over the wire)
-keep class com.tonihacks.qalam.data.** { *; }
-keep class com.tonihacks.qalam.domain.** { *; }
```

### How to debug R8 stripping

If a release build crashes with `ClassNotFoundException` or `NoSuchMethodException` that the debug build does not:

1. Find the class name in the stack trace.
2. Add `-keep class fully.qualified.ClassName { *; }` to `proguard-rules.pro`.
3. Rebuild.

You can also add `-printusage build/outputs/usage.txt` to see everything R8 removed.

---

## Step 3: Build the Release APK
Run the Gradle task to generate the signed APK:

```bash
./gradlew assembleRelease
```
The result will be at:
`app/build/outputs/apk/release/app-release.apk`

---

## Step 4: Install via ADB (Recommended)
This is the fastest way to get the app on your phone.

1.  Connect your phone (USB or Wireless ADB).
2.  Run:
    ```bash
    adb install -r app/build/outputs/apk/release/app-release.apk
    ```
    *The `-r` flag stands for "replace," allowing you to update the app without losing data.*

---

## Step 5: Manual Sideloading (Optional)
If you want to install it without a computer:

1.  Send the `app-release.apk` to your phone (via Telegram, Google Drive, or `adb push`).
2.  On the phone, open a File Manager and tap the APK.
3.  If prompted, enable **"Install unknown apps"** for that File Manager.

---

## Troubleshooting

### "App not installed as package appears to be invalid"
This usually happens if you try to install a **Release** build over a **Debug** build. Android security prevents this because the signing keys don't match.
*   **Fix:** Uninstall the existing Qalam app from your phone first, then try the `adb install` again.

### `SigningConfig "release" is missing required property "storePassword"`

`project.findProperty()` reads `gradle.properties`, not `local.properties`. If signing props live in `local.properties`, you must load that file explicitly (see Step 2 above). The properties exist on disk but Gradle never sees them via `findProperty`.

### "Supplied proguard configuration does not exist"

`app/proguard-rules.pro` is missing. See Step 2b — create the file, then rerun `assembleRelease`.

### "Cleartext traffic not permitted"
If you are testing the Release build against your Tailscale backend and it fails to connect:
*   Ensure your `network_security_config.xml` is correctly applied to the release build.
*   Check that your Tailscale IP is reachable from the phone.
