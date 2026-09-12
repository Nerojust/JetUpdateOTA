package com.nerojust.ota.core

import java.io.File

interface UpdateInstaller {
    suspend fun install(apkFile: File): InstallResult
}
