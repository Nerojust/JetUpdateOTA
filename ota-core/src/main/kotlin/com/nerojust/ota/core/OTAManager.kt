package com.nerojust.ota.core

import com.nerojust.ota.core.model.UpdateManifest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach
import java.io.File

class OTAManager(
    private val config: OTAConfig,
    private val checker: UpdateChecker,
    private val downloader: UpdateDownloader,
    private val installer: UpdateInstaller,
    private val stateStore: UpdateStateStore,
) {
    suspend fun checkForUpdate(
        currentVersionCode: Int,
        deviceId: String,
    ): UpdateCheckResult {
        val result = checker.fetchAndEvaluate(currentVersionCode, deviceId)
        val now = System.currentTimeMillis()
        val currentState = stateStore.currentState()
        val pendingVersionCode =
            if (result is UpdateCheckResult.Available) {
                result.manifest.versionCode
            } else {
                currentState.pendingVersionCode
            }
        stateStore.update(currentState.copy(lastCheckTimeMillis = now, pendingVersionCode = pendingVersionCode))
        return result
    }

    fun downloadUpdate(
        manifest: UpdateManifest,
        targetDir: File,
    ): Flow<DownloadProgress> =
        downloader.download(manifest, targetDir).onEach { progress ->
            if (progress is DownloadProgress.Complete) {
                val currentState = stateStore.currentState()
                stateStore.update(currentState.copy(downloadedApkPath = progress.apkFile.absolutePath))
            }
        }

    suspend fun installUpdate(apkFile: File): InstallResult {
        // InstallResult.Success means the install was successfully handed to the
        // system installer, not that it completed -- there is no completion
        // signal wired yet (no broadcast receiver for PackageInstallerSession's
        // commit), so pending state is intentionally left in place here rather
        // than cleared on a result that isn't actually confirmed.
        return installer.install(apkFile)
    }

    suspend fun currentState(): UpdateState = stateStore.currentState()
}
