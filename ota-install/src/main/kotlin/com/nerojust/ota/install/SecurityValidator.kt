package com.nerojust.ota.install

import java.io.File

class SecurityValidator(private val certificateReader: SigningCertificateReader) {

    fun verifySignatureMatches(apkFile: File): Boolean {
        val installed = certificateReader.installedAppCertificates()
        val candidate = certificateReader.candidateApkCertificates(apkFile)
        if (installed.isEmpty() || candidate.isEmpty()) return false
        if (installed.size != candidate.size) return false
        return installed.zip(candidate).all { (a, b) -> a.contentEquals(b) }
    }
}
