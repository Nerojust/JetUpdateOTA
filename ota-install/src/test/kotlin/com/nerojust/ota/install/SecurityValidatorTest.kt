package com.nerojust.ota.install

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.io.File

class SecurityValidatorTest {

    private val apkFile = File("/tmp/update.apk")
    private val certA = byteArrayOf(1, 2, 3)
    private val certB = byteArrayOf(4, 5, 6)

    @Test
    fun `matches when installed and candidate certificates are identical`() {
        val validator = SecurityValidator(FakeSigningCertificateReader(listOf(certA), listOf(certA)))

        assertTrue(validator.verifySignatureMatches(apkFile))
    }

    @Test
    fun `does not match when certificates differ`() {
        val validator = SecurityValidator(FakeSigningCertificateReader(listOf(certA), listOf(certB)))

        assertFalse(validator.verifySignatureMatches(apkFile))
    }

    @Test
    fun `does not match when either side has no certificates`() {
        val validator = SecurityValidator(FakeSigningCertificateReader(emptyList(), listOf(certA)))

        assertFalse(validator.verifySignatureMatches(apkFile))
    }
}
