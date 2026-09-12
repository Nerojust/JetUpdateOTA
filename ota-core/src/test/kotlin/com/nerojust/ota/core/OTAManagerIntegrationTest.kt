package com.nerojust.ota.core

import com.nerojust.ota.core.model.UpdateManifest
import com.nerojust.ota.testing.FakeUpdateChecker
import com.nerojust.ota.testing.FakeUpdateDownloader
import com.nerojust.ota.testing.FakeUpdateInstaller
import com.nerojust.ota.testing.FakeUpdateStateStore
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class OTAManagerIntegrationTest {
    @Test
    fun `full check-download-install flow succeeds using fakes`() =
        runTest {
            val manifest =
                UpdateManifest(
                    versionCode = 2,
                    versionName = "1.1.0",
                    apkUrl = "https://example.com/app.apk",
                    checksum = "abc123",
                    fileSize = 10,
                )
            val downloadedApk = File("/tmp/update_2.apk")
            val stateStore = FakeUpdateStateStore()
            val manager =
                OTAManager(
                    config = OTAConfig(manifestUrl = "https://example.com/api/update-manifest"),
                    checker = FakeUpdateChecker(UpdateCheckResult.Available(manifest)),
                    downloader =
                        FakeUpdateDownloader(
                            listOf(DownloadProgress.InProgress(5, 10), DownloadProgress.Complete(downloadedApk)),
                        ),
                    installer = FakeUpdateInstaller(InstallResult.Success),
                    stateStore = stateStore,
                )

            val checkResult = manager.checkForUpdate(currentVersionCode = 1, deviceId = "device-1")
            assertEquals(UpdateCheckResult.Available(manifest), checkResult)
            assertEquals(2, manager.currentState().pendingVersionCode)

            val progress = manager.downloadUpdate(manifest, File("/tmp")).toList()
            val complete = progress.last() as DownloadProgress.Complete
            assertEquals(downloadedApk.absolutePath, manager.currentState().downloadedApkPath)

            val installResult = manager.installUpdate(complete.apkFile)

            assertEquals(InstallResult.Success, installResult)
            // Install only hands off to the system installer; there is no completion
            // signal wired yet, so pending state is intentionally preserved rather
            // than cleared here (see OTAManager.installUpdate).
            assertEquals(2, manager.currentState().pendingVersionCode)
            assertEquals(downloadedApk.absolutePath, manager.currentState().downloadedApkPath)
        }
}
