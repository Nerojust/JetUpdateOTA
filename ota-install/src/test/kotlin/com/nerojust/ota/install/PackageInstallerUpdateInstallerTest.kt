package com.nerojust.ota.install

import com.nerojust.ota.core.InstallResult
import com.nerojust.ota.core.OTAException
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import java.io.File

@RunWith(RobolectricTestRunner::class)
class PackageInstallerUpdateInstallerTest {

    private val context = RuntimeEnvironment.getApplication()

    @Test
    fun `fails when the apk file does not exist`() = runTest {
        val validator = SecurityValidator(FakeSigningCertificateReader(listOf(byteArrayOf(1)), listOf(byteArrayOf(1))))
        val installer = PackageInstallerUpdateInstaller(context, validator)

        val result = installer.install(File(context.cacheDir, "does-not-exist.apk"))

        assertTrue(result is InstallResult.Failed)
        assertTrue((result as InstallResult.Failed).error is OTAException.InstallFailed)
    }

    @Test
    fun `fails when the signature does not match the installed app`() = runTest {
        val apkFile = File(context.cacheDir, "update.apk").apply { writeText("apk-bytes") }
        val validator = SecurityValidator(
            FakeSigningCertificateReader(listOf(byteArrayOf(1)), listOf(byteArrayOf(2)))
        )
        val installer = PackageInstallerUpdateInstaller(context, validator)

        val result = installer.install(apkFile)

        assertTrue(result is InstallResult.Failed)
        assertTrue((result as InstallResult.Failed).error is OTAException.SignatureInvalid)
    }
}
