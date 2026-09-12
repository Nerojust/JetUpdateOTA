package com.nerojust.ota.install

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import java.io.File

class PackageManagerSigningCertificateReader(private val context: Context) : SigningCertificateReader {

    override fun installedAppCertificates(): List<ByteArray> =
        signingCertificatesOf(context.packageManager.getPackageInfoCompat(context.packageName))

    override fun candidateApkCertificates(apkFile: File): List<ByteArray> =
        signingCertificatesOf(context.packageManager.getPackageArchiveInfoCompat(apkFile.absolutePath))

    private fun signingCertificatesOf(info: PackageInfo?): List<ByteArray> {
        if (info == null) return emptyList()
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.signingCertificateHistory?.map { it.toByteArray() } ?: emptyList()
        } else {
            @Suppress("DEPRECATION")
            info.signatures?.map { it.toByteArray() } ?: emptyList()
        }
    }
}

private fun PackageManager.getPackageInfoCompat(packageName: String): PackageInfo? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        getPackageInfo(packageName, PackageManager.GET_SIGNING_CERTIFICATES)
    } else {
        @Suppress("DEPRECATION")
        getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
    }

private fun PackageManager.getPackageArchiveInfoCompat(apkPath: String): PackageInfo? =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNING_CERTIFICATES)
    } else {
        @Suppress("DEPRECATION")
        getPackageArchiveInfo(apkPath, PackageManager.GET_SIGNATURES)
    }
