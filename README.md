# JetUpdateOTA

[![Android CI](https://github.com/nerojust/JetUpdateOTA/actions/workflows/android.yml/badge.svg)](https://github.com/nerojust/JetUpdateOTA/actions/workflows/android.yml)
[![Backend CI](https://github.com/nerojust/JetUpdateOTA/actions/workflows/backend.yml/badge.svg)](https://github.com/nerojust/JetUpdateOTA/actions/workflows/backend.yml)
[![License: Apache 2.0](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.10-7F52FF.svg)](https://kotlinlang.org)

A Kotlin library that lets your Android app update itself — no Play
Store needed. It checks for a new version, downloads it, makes sure the
file is safe, and installs it. A small demo backend is included so you
can try the whole thing without setting anything up yourself.

## Why you might want this

Say you built an app for your company's warehouse scanners, or an app
you hand out as an APK instead of through Play. Sooner or later someone
asks: "how do we push an update to everyone's device?"

The usual answers are:

- Email everyone a new APK file. Forever. By hand.
- Build your own update system from scratch, and slowly discover all
  the annoying details: what if the download gets interrupted? What if
  the file is corrupted? What if someone swaps in a fake APK?

JetUpdateOTA is the third option: those problems, already solved, so
you can just use it.

## How to use it

### 1. Add it to your project

Not on Maven Central yet — that's on the to-do list. For now, add it as
a Gradle composite build:

```kotlin
// settings.gradle.kts
includeBuild("../JetUpdateOTA") {
    dependencySubstitution {
        substitute(module("com.nerojust:ota-core")).using(project(":ota-core"))
        // add the other ota-* modules the same way
    }
}
```

Then, in your app's `build.gradle.kts`:

```kotlin
dependencies {
    implementation(project(":ota-core"))
    implementation(project(":ota-network"))
    implementation(project(":ota-download"))
    implementation(project(":ota-install"))
    implementation(libs.okhttp)
}
```

### 2. Update your AndroidManifest.xml

Two permissions and a `FileProvider` — these have to live in your app,
not the library:

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

### 3. Set it up once

```kotlin
import com.nerojust.ota.core.OTAConfig
import com.nerojust.ota.core.OTAManager
import com.nerojust.ota.download.defaultUpdateDownloader
import com.nerojust.ota.install.defaultUpdateInstaller
import com.nerojust.ota.install.defaultUpdateStateStore
import com.nerojust.ota.network.defaultUpdateChecker
import okhttp3.OkHttpClient

fun createOtaManager(context: Context, manifestUrl: String): OTAManager {
    val config = OTAConfig(manifestUrl = manifestUrl) // must start with https://
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

### 4. Check for and install an update

```kotlin
when (val result = manager.checkForUpdate(currentVersionCode = BuildConfig.VERSION_CODE, deviceId = deviceId)) {
    is UpdateCheckResult.Available -> {
        manager.downloadUpdate(result.manifest, targetDir = context.cacheDir)
            .collect { progress ->
                when (progress) {
                    is DownloadProgress.Complete -> manager.installUpdate(progress.apkFile)
                    is DownloadProgress.Failed -> Unit // show an error
                    is DownloadProgress.InProgress -> Unit // update a progress bar
                }
            }
    }
    UpdateCheckResult.NotInRollout, UpdateCheckResult.UpToDate -> Unit // nothing to do
    is UpdateCheckResult.Failed -> Unit // handle result.error
}
```

Want to see this actually run? Check out [`app/`](app/) —
[`OtaComposition.kt`](app/src/main/java/com/nerojust/jetupdateota/ota/OtaComposition.kt)
is this exact code, already wired up.

### 5. Try the demo backend

The app needs a server to talk to. [`backend-reference/`](backend-reference/)
is a small one you can run locally in a minute:

```bash
cd backend-reference
npm install
npm run dev
```

It answers one simple question: "is there a newer version for this
device?" To publish a new version, run a script — there's no public
upload button, on purpose:

```bash
npm run publish-release -- <apkPath> <versionCode> <versionName> <apkBaseUrl>
```

## What to expect

A few honest notes before you rely on this:

- **The server has the final say.** If it says "not for you yet" or
  "you're up to date," the app just listens — it doesn't try to
  second-guess the answer.
- **"Install successful" means "handed off to Android," not "100%
  done."** There's no way yet to confirm the install actually finished.
  That needs a real phone to build and test properly, so it's simply
  not built yet — not hidden, just not there.
- **Every update is checked before it's installed.** If the file doesn't
  match the app that's already on the phone, it's rejected. No
  exceptions, no settings to turn this off.
- **This is a starting point, not a finished product.** No Maven
  Central release yet, no "Check for updates" button in the demo app,
  and the demo backend has no login system — it's meant for trying
  things out, not for running as-is in production.

Everything above is also tracked in [CHANGELOG.md](CHANGELOG.md).

## Development

```bash
./gradlew test                       # Android tests
./gradlew ktlintCheck                # Kotlin style check
cd backend-reference && npm test     # backend tests
cd backend-reference && npm run lint
```

See [CONTRIBUTING.md](CONTRIBUTING.md) for the full guide, and
[SECURITY.md](SECURITY.md) if you find a security issue — please report
that privately instead of opening a public issue.

## License

Apache 2.0 — see [LICENSE](LICENSE).
