package com.nerojust.ota.install

import android.content.Context
import com.nerojust.ota.core.InstallResult
import com.nerojust.ota.core.OTAException
import com.nerojust.ota.core.UpdateInstaller
import com.nerojust.ota.core.UpdateStateStore
import java.io.File
import java.io.FileNotFoundException

class PackageInstallerUpdateInstaller(
    private val context: Context,
    private val securityValidator: SecurityValidator,
) : UpdateInstaller {

    override suspend fun install(apkFile: File): InstallResult {
        if (!apkFile.exists()) {
            return InstallResult.Failed(OTAException.InstallFailed(FileNotFoundException(apkFile.absolutePath)))
        }
        if (!securityValidator.verifySignatureMatches(apkFile)) {
            return InstallResult.Failed(OTAException.SignatureInvalid())
        }
        return try {
            PackageInstallerSession.commit(context, apkFile)
            InstallResult.Success
        } catch (e: Exception) {
            InstallResult.Failed(OTAException.InstallFailed(e))
        }
    }
}

fun defaultUpdateInstaller(context: Context): UpdateInstaller =
    PackageInstallerUpdateInstaller(context, SecurityValidator(PackageManagerSigningCertificateReader(context)))

fun defaultUpdateStateStore(context: Context): UpdateStateStore = SharedPrefsUpdateStateStore(context)
