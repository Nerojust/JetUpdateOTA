package com.nerojust.ota.core

import com.nerojust.ota.core.model.UpdateManifest
import kotlinx.coroutines.flow.Flow
import java.io.File

interface UpdateDownloader {
    fun download(manifest: UpdateManifest, targetDir: File): Flow<DownloadProgress>
}
