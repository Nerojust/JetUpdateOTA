package com.nerojust.ota.install

import java.io.File

interface SigningCertificateReader {
    fun installedAppCertificates(): List<ByteArray>
    fun candidateApkCertificates(apkFile: File): List<ByteArray>
}
