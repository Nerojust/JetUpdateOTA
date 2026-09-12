package com.nerojust.ota.testing

import com.nerojust.ota.core.InstallResult
import com.nerojust.ota.core.UpdateInstaller
import java.io.File

class FakeUpdateInstaller(private val result: InstallResult) : UpdateInstaller {
    override suspend fun install(apkFile: File): InstallResult = result
}
