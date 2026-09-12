# JetUpdateOTA

[![Android CI](https://github.com/nerojust/JetUpdateOTA/actions/workflows/android.yml/badge.svg)](https://github.com/nerojust/JetUpdateOTA/actions/workflows/android.yml)
[![Backend CI](https://github.com/nerojust/JetUpdateOTA/actions/workflows/backend.yml/badge.svg)](https://github.com/nerojust/JetUpdateOTA/actions/workflows/backend.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg)](https://kotlinlang.org)

A Kotlin library that updates your Android app **without asking Google
Play for permission first.** Check for an update, download it with
actual resume support, verify it isn't garbage or malware, and hand it
to the system installer. A tiny reference backend is included so you
can watch the whole loop work before you trust it with your own app.

## Why this exists

You built an internal tool, an app for company-managed tablets, or
something you sideload on purpose — and now you need it to update
itself. Your options, as commonly practiced, are:

1. Text your users a new APK link every release, and hope.
2. Write "just fetch a JSON file and download an APK, how hard can it
   be" — and then, several bugs later, discover the answer: resumable
   downloads, checksum verification, signature verification, rollout
   percentages, and "why did `InstallResult.Success` fire before the
   user even tapped Install" are all real, and all yours now.
3. Use this instead.

JetUpdateOTA is option 3. It's the boring, already-solved version of
option 2 — someone (us) already hit the checksum-mismatch bug and the
"wait, the client and server disagree on rollout" bug, so you don't
have to. It won't publish to Google Play for you either, but that was
never the point.

## How to use it

### Install

Not on Maven Central yet (see [CHANGELOG](CHANGELOG.md) — it's on the
list). For now, pull it in as a Gradle composite build:

```kotlin
// settings.gradle.kts
includeBuild("../JetUpdateOTA") {
    dependencySubstitution {
        substitute(module("com.nerojust:ota-core")).using(project(":ota-core"))
        // ...and whichever other ota-* modules you actually need
    }
}
```

Then in your app module:

```kotlin
dependencies {
    implementation(project(":ota-core"))
    implementation(project(":ota-network"))
    implementation(project(":ota-download"))
    implementation(project(":ota-install"))
    implementation(libs.okhttp) // bring your own client — we don't hide one inside
}
```

And in your manifest (the library can't declare these for you — permissions
and `FileProvider` authorities are per-app, not per-library):

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

### Wire it up

No singleton, no `OTAManager.getInstance()` lurking in a static field —
you build one and hold onto it yourself:

```kotlin
import com.nerojust.ota.core.OTAConfig
import com.nerojust.ota.core.OTAManager
import com.nerojust.ota.download.defaultUpdateDownloader
import com.nerojust.ota.install.defaultUpdateInstaller
import com.nerojust.ota.install.defaultUpdateStateStore
import com.nerojust.ota.network.defaultUpdateChecker
import okhttp3.OkHttpClient

fun createOtaManager(context: Context, manifestUrl: String): OTAManager {
    val config = OTAConfig(manifestUrl = manifestUrl) // HTTPS only — it will yell at you otherwise
    val httpClient = OkHttpClient()
    return OTAManager(
        config = config,
        checker = defaultUpdateChecker(httpClient, config.manifestUrl),
        downloader = defaultUpdateDownloader(httpClient),
        installer = defaultUpdateInstaller(context),
        stateStore = defaultUpdateStateStore(context),
    )
}
```

### Use it

```kotlin
when (val result = manager.checkForUpdate(currentVersionCode = BuildConfig.VERSION_CODE, deviceId = deviceId)) {
    is UpdateCheckResult.Available -> {
        manager.downloadUpdate(result.manifest, targetDir = context.cacheDir)
            .collect { progress ->
                when (progress) {
                    is DownloadProgress.Complete -> manager.installUpdate(progress.apkFile)
                    is DownloadProgress.Failed -> /* tell the user, don't just log it */ Unit
                    is DownloadProgress.InProgress -> /* update a progress bar, we did the hard part */ Unit
                }
            }
    }
    UpdateCheckResult.NotInRollout, UpdateCheckResult.UpToDate -> Unit // nothing to see here
    is UpdateCheckResult.Failed -> /* handle result.error, it's a real sealed type, not a string */ Unit
}
```

Want to see it actually running instead of taking our word for it? Look
at [`app/`](app/) —
[`OtaComposition.kt`](app/src/main/java/com/nerojust/jetupdateota/ota/OtaComposition.kt)
is exactly the snippet above, compiled against the real modules, not a
docs fantasy.

### The reference backend

A server has to answer `HttpUpdateChecker`'s questions somehow.
[`backend-reference/`](backend-reference/) is a small Node/TypeScript one
that does, so you're not staring at a client with nothing to talk to:

```bash
cd backend-reference
npm install
npm run dev            # or: docker compose up --build, if you prefer boxes to processes
```

`GET /api/update-manifest?app_version=1&device_id=abc123` answers
`{"has_update": true, "manifest": {...}}` or
`{"has_update": false, "reason": "up_to_date" | "not_in_rollout"}` — and
the client trusts that answer completely, on purpose (see below).
Publishing a release is a local script, not a wide-open endpoint anyone
on the internet can hit:

```bash
npm run publish-release -- <apkPath> <versionCode> <versionName> <apkBaseUrl>
```

## What to expect

Read this part. It'll save you a confused GitHub issue later.

- **The server decides, the client obeys.** Rollout percentage and
  "is this actually newer" are both decided server-side. The client
  doesn't re-litigate the decision with its own copy of the logic —
  we tried that, the two disagreed with each other, and "silently
  wrong 60% of the time" is not a feature.
- **`InstallResult.Success` means "handed to the system installer,"
  not "definitely installed."** There's no confirmation receiver wired
  up yet — that needs a real device to build and test properly, so
  it's honestly listed as not-done rather than quietly assumed. Don't
  build a "great, we're up to date" banner on top of this result.
- **Signature verification happens before every install, no exceptions.**
  If the new APK isn't signed the same as what's installed, it doesn't
  reach `PackageInstaller`. This is not configurable, and that's
  deliberate.
- **This isn't a Play Store replacement's marketing page.** No Maven
  Central artifact yet, no update-checking UI in the example app (the
  wiring compiles, nobody's built a "Check for updates" button on top
  of it), and the reference backend has zero authentication because
  it's a reference, not something you should point at the public
  internet unmodified.

None of that is secret — it's all tracked in [CHANGELOG.md](CHANGELOG.md)
under "Planned," and every module's tests document exactly what is and
isn't covered.

## Development

```bash
./gradlew test                       # all Android modules
./gradlew ktlintCheck                # Kotlin style
cd backend-reference && npm test     # backend
cd backend-reference && npm run lint
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full workflow (spoiler:
tests come first), and [SECURITY.md](SECURITY.md) if you've found a way
to make the signature check say yes when it should say no — that one's
important, please don't file it as a regular issue.

## License

Apache 2.0 — see [LICENSE](LICENSE). Use it, fork it, ship it; just
don't blame us if you skip the "read the manifest permissions" step
above and then wonder why nothing installs.
