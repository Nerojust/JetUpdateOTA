package com.nerojust.ota.download

import com.nerojust.ota.core.DownloadProgress
import com.nerojust.ota.core.model.UpdateManifest
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.security.MessageDigest

class HttpUpdateDownloaderTest {
    private val server = MockWebServer()
    private val httpClient = OkHttpClient()
    private val downloader = HttpUpdateDownloader(httpClient)

    @TempDir
    lateinit var targetDir: File

    @BeforeEach
    fun startServer() {
        server.start()
    }

    @AfterEach
    fun stopServer() {
        server.shutdown()
    }

    private fun sha256(content: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(content.toByteArray())
        return digest.joinToString("") { "%02x".format(it) }
    }

    @Test
    fun `downloads the full file and emits Complete with a matching checksum`() =
        runTest {
            val content = "HELLOWORLD"
            server.enqueue(MockResponse().setBody(content))
            val manifest =
                UpdateManifest(
                    versionCode = 2,
                    versionName = "1.1.0",
                    apkUrl = server.url("/app.apk").toString(),
                    checksum = sha256(content),
                    fileSize = content.length.toLong(),
                )

            val events = downloader.download(manifest, targetDir).toList()

            val complete = events.last() as DownloadProgress.Complete
            assertEquals(content, complete.apkFile.readText())
        }

    @Test
    fun `resumes from an existing partial file using a Range request`() =
        runTest {
            val fullContent = "HELLOWORLD"
            val partial = "HELLO"
            val remaining = "WORLD"
            val targetFile = File(targetDir, "update_2.apk")
            targetFile.writeText(partial)

            server.enqueue(MockResponse().setResponseCode(206).setBody(remaining))
            val manifest =
                UpdateManifest(
                    versionCode = 2,
                    versionName = "1.1.0",
                    apkUrl = server.url("/app.apk").toString(),
                    checksum = sha256(fullContent),
                    fileSize = fullContent.length.toLong(),
                )

            val events = downloader.download(manifest, targetDir).toList()

            val complete = events.last() as DownloadProgress.Complete
            assertEquals(fullContent, complete.apkFile.readText())
            val recordedRequest = server.takeRequest()
            assertEquals("bytes=5-", recordedRequest.getHeader("Range"))
        }

    @Test
    fun `restarts from scratch when the server ignores the Range header and returns 200`() =
        runTest {
            val fullContent = "HELLOWORLD"
            val partial = "HELLO"
            val targetFile = File(targetDir, "update_2.apk")
            targetFile.writeText(partial)

            server.enqueue(MockResponse().setResponseCode(200).setBody(fullContent))
            val manifest =
                UpdateManifest(
                    versionCode = 2,
                    versionName = "1.1.0",
                    apkUrl = server.url("/app.apk").toString(),
                    checksum = sha256(fullContent),
                    fileSize = fullContent.length.toLong(),
                )

            val events = downloader.download(manifest, targetDir).toList()

            val complete = events.last() as DownloadProgress.Complete
            assertEquals(fullContent, complete.apkFile.readText())
            val inProgressEvents = events.filterIsInstance<DownloadProgress.InProgress>()
            assertTrue(inProgressEvents.isNotEmpty())
            inProgressEvents.forEach { event ->
                assertTrue(event.bytesDownloaded <= complete.apkFile.length())
            }
        }

    @Test
    fun `emits Failed and deletes the file when the checksum does not match`() =
        runTest {
            server.enqueue(MockResponse().setBody("HELLOWORLD"))
            val manifest =
                UpdateManifest(
                    versionCode = 2,
                    versionName = "1.1.0",
                    apkUrl = server.url("/app.apk").toString(),
                    checksum = "not-the-real-checksum",
                    fileSize = 10,
                )

            val events = downloader.download(manifest, targetDir).toList()

            assertTrue(events.last() is DownloadProgress.Failed)
            assertFalse(File(targetDir, "update_2.apk").exists())
        }

    @Test
    fun `emits Failed when the server responds with an error status`() =
        runTest {
            server.enqueue(MockResponse().setResponseCode(404))
            val manifest =
                UpdateManifest(
                    versionCode = 2,
                    versionName = "1.1.0",
                    apkUrl = server.url("/app.apk").toString(),
                    checksum = "irrelevant",
                    fileSize = 10,
                )

            val events = downloader.download(manifest, targetDir).toList()

            assertTrue(events.last() is DownloadProgress.Failed)
        }
}
