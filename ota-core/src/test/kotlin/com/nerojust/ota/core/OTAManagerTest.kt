package com.nerojust.ota.core

import com.nerojust.ota.core.model.UpdateManifest
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.File

class OTAManagerTest {

    private val manifest = UpdateManifest(
        versionCode = 2,
        versionName = "1.1.0",
        apkUrl = "https://example.com/app.apk",
        checksum = "abc123",
        fileSize = 10,
    )

    private val config = OTAConfig(manifestUrl = "https://example.com/api/update-manifest")

    @Test
    fun `checkForUpdate delegates to the checker`() = runTest {
        val checker = mockk<UpdateChecker>()
        coEvery { checker.fetchAndEvaluate(1, "device-1") } returns UpdateCheckResult.UpToDate
        val stateStore = mockk<UpdateStateStore>()
        coEvery { stateStore.currentState() } returns UpdateState()
        coEvery { stateStore.update(any()) } returns Unit
        val manager = OTAManager(config, checker, mockk(), mockk(), stateStore)

        val result = manager.checkForUpdate(currentVersionCode = 1, deviceId = "device-1")

        assertEquals(UpdateCheckResult.UpToDate, result)
    }

    @Test
    fun `checkForUpdate records the pending version when an update is available`() = runTest {
        val checker = mockk<UpdateChecker>()
        coEvery { checker.fetchAndEvaluate(any(), any()) } returns UpdateCheckResult.Available(manifest)
        val stateStore = mockk<UpdateStateStore>()
        coEvery { stateStore.currentState() } returns UpdateState()
        coEvery { stateStore.update(any()) } returns Unit
        val manager = OTAManager(config, checker, mockk(), mockk(), stateStore)

        manager.checkForUpdate(currentVersionCode = 1, deviceId = "device-1")

        coVerify { stateStore.update(match { it.pendingVersionCode == 2 }) }
    }

    @Test
    fun `checkForUpdate preserves the existing pending version when up to date`() = runTest {
        val checker = mockk<UpdateChecker>()
        coEvery { checker.fetchAndEvaluate(any(), any()) } returns UpdateCheckResult.UpToDate
        val stateStore = mockk<UpdateStateStore>()
        coEvery { stateStore.currentState() } returns UpdateState(pendingVersionCode = 5)
        coEvery { stateStore.update(any()) } returns Unit
        val manager = OTAManager(config, checker, mockk(), mockk(), stateStore)

        manager.checkForUpdate(currentVersionCode = 1, deviceId = "device-1")

        coVerify { stateStore.update(match { it.pendingVersionCode == 5 }) }
    }

    @Test
    fun `downloadUpdate delegates to the downloader and records the downloaded path on completion`() = runTest {
        val downloader = mockk<UpdateDownloader>()
        val targetDir = File("/tmp")
        val completedFile = File("/tmp/update_2.apk")
        every { downloader.download(manifest, targetDir) } returns
            flowOf(DownloadProgress.InProgress(5, 10), DownloadProgress.Complete(completedFile))
        val stateStore = mockk<UpdateStateStore>()
        coEvery { stateStore.currentState() } returns UpdateState()
        coEvery { stateStore.update(any()) } returns Unit
        val manager = OTAManager(config, mockk(), downloader, mockk(), stateStore)

        val events = manager.downloadUpdate(manifest, targetDir).toList()

        assertEquals(2, events.size)
        coVerify { stateStore.update(match { it.downloadedApkPath == completedFile.absolutePath }) }
    }

    @Test
    fun `installUpdate does not clear state on success`() = runTest {
        val installer = mockk<UpdateInstaller>()
        val apkFile = File("/tmp/update_2.apk")
        coEvery { installer.install(apkFile) } returns InstallResult.Success
        val stateStore = mockk<UpdateStateStore>(relaxed = true)
        val manager = OTAManager(config, mockk(), mockk(), installer, stateStore)

        val result = manager.installUpdate(apkFile)

        assertEquals(InstallResult.Success, result)
        coVerify(exactly = 0) { stateStore.clear() }
        coVerify(exactly = 0) { stateStore.update(any()) }
    }

    @Test
    fun `installUpdate does not clear state on failure`() = runTest {
        val installer = mockk<UpdateInstaller>()
        val apkFile = File("/tmp/update_2.apk")
        val failure = InstallResult.Failed(OTAException.SignatureInvalid())
        coEvery { installer.install(apkFile) } returns failure
        val stateStore = mockk<UpdateStateStore>(relaxed = true)
        val manager = OTAManager(config, mockk(), mockk(), installer, stateStore)

        val result = manager.installUpdate(apkFile)

        assertEquals(failure, result)
        coVerify(exactly = 0) { stateStore.clear() }
    }

    @Test
    fun `currentState delegates to the state store`() = runTest {
        val stateStore = mockk<UpdateStateStore>()
        val state = UpdateState(pendingVersionCode = 2)
        coEvery { stateStore.currentState() } returns state
        val manager = OTAManager(config, mockk(), mockk(), mockk(), stateStore)

        assertEquals(state, manager.currentState())
    }
}
