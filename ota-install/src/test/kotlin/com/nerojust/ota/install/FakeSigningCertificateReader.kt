package com.nerojust.ota.install

import java.io.File

internal class FakeSigningCertificateReader(
    private val installed: List<ByteArray>,
    private val candidate: List<ByteArray>,
) : SigningCertificateReader {
    override fun installedAppCertificates(): List<ByteArray> = installed
    override fun candidateApkCertificates(apkFile: File): List<ByteArray> = candidate
}
