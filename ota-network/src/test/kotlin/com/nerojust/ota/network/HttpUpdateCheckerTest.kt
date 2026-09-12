package com.nerojust.ota.network

import com.nerojust.ota.core.OTAException
import com.nerojust.ota.core.UpdateCheckResult
import kotlinx.coroutines.test.runTest
import okhttp3.OkHttpClient
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class HttpUpdateCheckerTest {

    private val server = MockWebServer()
    private val httpClient = OkHttpClient()

    @BeforeEach
    fun startServer() {
        server.start()
    }

    @AfterEach
    fun stopServer() {
        server.shutdown()
    }

    private fun checker() = HttpUpdateChecker(httpClient, server.url("/api/update-manifest").toString())

    @Test
    fun `returns Available when server reports an update`() = runTest {
        server.enqueue(
            MockResponse().setBody(
                """{"has_update":true,"manifest":{"version_code":2,"version_name":"1.1.0","apk_url":"https://cdn.example.com/app.apk","checksum":"abc","file_size":10,"rollout_percentage":100}}"""
            )
        )

        val result = checker().fetchAndEvaluate(currentVersionCode = 1, deviceId = "device-1")

        assertTrue(result is UpdateCheckResult.Available)
        assertEquals(2, (result as UpdateCheckResult.Available).manifest.versionCode)
    }

    @Test
    fun `returns UpToDate when server reports no update with no reason`() = runTest {
        server.enqueue(MockResponse().setBody("""{"has_update":false}"""))

        val result = checker().fetchAndEvaluate(currentVersionCode = 1, deviceId = "device-1")

        assertEquals(UpdateCheckResult.UpToDate, result)
    }

    @Test
    fun `returns UpToDate when server reports reason up_to_date`() = runTest {
        server.enqueue(MockResponse().setBody("""{"has_update":false,"reason":"up_to_date"}"""))

        val result = checker().fetchAndEvaluate(currentVersionCode = 1, deviceId = "device-1")

        assertEquals(UpdateCheckResult.UpToDate, result)
    }

    @Test
    fun `returns NotInRollout when server reports reason not_in_rollout`() = runTest {
        server.enqueue(MockResponse().setBody("""{"has_update":false,"reason":"not_in_rollout"}"""))

        val result = checker().fetchAndEvaluate(currentVersionCode = 1, deviceId = "device-1")

        assertEquals(UpdateCheckResult.NotInRollout, result)
    }

    @Test
    fun `sends app_version and device_id as query parameters`() = runTest {
        server.enqueue(MockResponse().setBody("""{"has_update":false}"""))

        checker().fetchAndEvaluate(currentVersionCode = 3, deviceId = "device-42")

        val recordedRequest = server.takeRequest()
        assertEquals("/api/update-manifest?app_version=3&device_id=device-42", recordedRequest.path)
    }

    @Test
    fun `returns Failed when server responds with an error status`() = runTest {
        server.enqueue(MockResponse().setResponseCode(500))

        val result = checker().fetchAndEvaluate(currentVersionCode = 1, deviceId = "device-1")

        assertTrue(result is UpdateCheckResult.Failed)
        assertTrue((result as UpdateCheckResult.Failed).error is OTAException.NetworkError)
    }

    @Test
    fun `returns Failed when the connection fails`() = runTest {
        server.shutdown()

        val result = checker().fetchAndEvaluate(currentVersionCode = 1, deviceId = "device-1")

        assertTrue(result is UpdateCheckResult.Failed)
    }

    @Test
    fun `returns Failed when the response body is malformed JSON`() = runTest {
        server.enqueue(MockResponse().setBody("not json"))

        val result = checker().fetchAndEvaluate(currentVersionCode = 1, deviceId = "device-1")

        assertTrue(result is UpdateCheckResult.Failed)
    }
}
