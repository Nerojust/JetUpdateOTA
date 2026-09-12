package com.nerojust.ota.core

import java.io.File

sealed interface DownloadProgress {
    data class InProgress(val bytesDownloaded: Long, val totalBytes: Long) : DownloadProgress

    data class Complete(val apkFile: File) : DownloadProgress

    data class Failed(val error: OTAException) : DownloadProgress
}
