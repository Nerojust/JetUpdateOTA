package com.nerojust.ota.testing

import com.nerojust.ota.core.DownloadProgress
import com.nerojust.ota.core.UpdateDownloader
import com.nerojust.ota.core.model.UpdateManifest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import java.io.File

class FakeUpdateDownloader(private val emissions: List<DownloadProgress>) : UpdateDownloader {
    override fun download(
        manifest: UpdateManifest,
        targetDir: File,
    ): Flow<DownloadProgress> = emissions.asFlow()
}
