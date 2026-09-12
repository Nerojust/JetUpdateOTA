# JetUpdateOTA

[![Android CI](https://github.com/nerojust/JetUpdateOTA/actions/workflows/android.yml/badge.svg)](https://github.com/nerojust/JetUpdateOTA/actions/workflows/android.yml)
[![Backend CI](https://github.com/nerojust/JetUpdateOTA/actions/workflows/backend.yml/badge.svg)](https://github.com/nerojust/JetUpdateOTA/actions/workflows/backend.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg)](https://kotlinlang.org)

A Kotlin library for managing custom APK updates on Android **without a
Play Store dependency** — check for updates, download with resume
support, verify the result, and hand it to the system installer. Ships
with a small reference backend so you can see the whole loop working
end to end.

## Why

Play Store distribution isn't always the right fit — internal tools,
enterprise/managed devices, sideloaded apps, or products distributed
outside Google Play all need their own update mechanism. JetUpdateOTA
gives you the client-side building blocks for that, with a security
model (signature verification before install, checksum verification
before that) that isn't an afterthought.

## Architecture

```
┌─────────────┐
│  Your App   │
└──────┬──────┘
       │ composes
       ▼
┌─────────────────────────────────────────────────────┐
│ ota-core        OTAManager, models, the 4            │
│                  collaborator interfaces              │
│                  (no Android dependency)              │
└──────┬───────────┬───────────┬───────────────────────┘
       │           │           │
       ▼           ▼           ▼
┌───────────┐ ┌───────────┐ ┌────────────────────────┐
│ota-network│ │ota-download│ │ota-install             │
│           │ │            │ │SecurityValidator,      │
│HTTP manifest│ │Resumable  │ │PackageInstaller,       │
│fetch, server-│ │download + │ │SharedPreferences state │
│authoritative │ │SHA-256    │ │(the one Android-       │
│rollout       │ │checksum   │ │dependent module)       │
└───────────┘ └───────────┘ └────────────────────────┘

ota-testing: in-memory fakes of the 4 interfaces, for your own tests.
```

`ota-core` depends on nothing else in this repo — it defines the public
API and the interfaces the other modules implement. You (the consumer)
compose the real implementations together via each module's
`defaultXxx()` factory function; there's no hidden singleton and no
Android `Context` threaded through the core API.

## Install

Not yet published to Maven Central (tracked as future work — see
[CHANGELOG](CHANGELOG.md)). For now, include this repo as a Gradle
composite build or copy the `ota-*` modules directly:

```kotlin
// settings.gradle.kts
includeBuild("../JetUpdateOTA") {
    dependencySubstitution {
        substitute(module("com.nerojust:ota-core")).using(project(":ota-core"))
        // ...and the other ota-* modules you need
    }
}
```

Add to your app module:

```kotlin
dependencies {
    implementation(project(":ota-core"))
    implementation(project(":ota-network"))
    implementation(project(":ota-download"))
    implementation(project(":ota-install"))
    implementation(libs.okhttp) // you bring your own OkHttpClient
}
```

Your app's manifest needs:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.REQUEST_INSTALL_PACKAGES" />

<provider
    android:name="androidx.core.content.FileProvider"
    android:authorities="${applicationId}.fileprovider"
    android:exported="false"
    android:grantUriPermissions="true">
    <meta-data
        android:name="android.support.FILE_PROVIDER_PATHS"
        android:resource="@xml/file_paths" />
</provider>
```

## Quick start

```kotlin
import com.nerojust.ota.core.OTAConfig
import com.nerojust.ota.core.OTAManager
import com.nerojust.ota.download.defaultUpdateDownloader
import com.nerojust.ota.install.defaultUpdateInstaller
import com.nerojust.ota.install.defaultUpdateStateStore
import com.nerojust.ota.network.defaultUpdateChecker
import okhttp3.OkHttpClient

fun createOtaManager(context: Context, manifestUrl: String): OTAManager {
    val config = OTAConfig(manifestUrl = manifestUrl) // must be HTTPS
    val httpClient = OkHttpClient()
    return OTAManager(
        config = config,
        checker = defaultUpdateChecker(httpClient, config.manifestUrl),
        downloader = defaultUpdateDownloader(httpClient),
        installer = defaultUpdateInstaller(context),
        stateStore = defaultUpdateStateStore(context),
    )
}

// Elsewhere, in a coroutine scope:
when (val result = manager.checkForUpdate(currentVersionCode = BuildConfig.VERSION_CODE, deviceId = deviceId)) {
    is UpdateCheckResult.Available -> {
        manager.downloadUpdate(result.manifest, targetDir = context.cacheDir)
            .collect { progress ->
                when (progress) {
                    is DownloadProgress.Complete -> manager.installUpdate(progress.apkFile)
                    is DownloadProgress.Failed -> /* handle */ Unit
                    is DownloadProgress.InProgress -> /* update UI */ Unit
                }
            }
    }
    UpdateCheckResult.NotInRollout, UpdateCheckResult.UpToDate -> Unit
    is UpdateCheckResult.Failed -> /* handle result.error */ Unit
}
```

A working example lives in [`app/`](app/) —
[`OtaComposition.kt`](app/src/main/java/com/nerojust/jetupdateota/ota/OtaComposition.kt)
is the composition root shown above, wired for real.

## Reference backend

[`backend-reference/`](backend-reference/) is a small Node/TypeScript
server implementing the manifest contract the client expects.

```bash
cd backend-reference
npm install
npm run dev            # or: docker compose up --build
```

`GET /api/update-manifest?app_version=1&device_id=abc123` returns
`{"has_update": true, "manifest": {...}}` or
`{"has_update": false, "reason": "up_to_date" | "not_in_rollout"}`.
Publishing a new version is a local script, not an endpoint:

```bash
npm run publish-release -- <apkPath> <versionCode> <versionName> <apkBaseUrl>
```

See [`backend-reference/`](backend-reference/) for the full route/service
breakdown.

## Development

```bash
./gradlew test                      # all Android modules
./gradlew ktlintCheck                # Kotlin style
cd backend-reference && npm test    # backend
cd backend-reference && npm run lint
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full workflow, and
[SECURITY.md](SECURITY.md) if you've found a vulnerability.

## What's out of scope (for now)

- Maven Central publishing / signing.
- A real completion signal for installs — `InstallResult.Success` means
  "handed to the system installer," not "confirmed complete." Wiring a
  `BroadcastReceiver` for real completion tracking needs a physical
  device to test properly and is tracked as future work.
- The example app's actual update-checking UI (the composition root
  exists and compiles; nothing calls it yet).

## License

Apache 2.0 — see [LICENSE](LICENSE).
