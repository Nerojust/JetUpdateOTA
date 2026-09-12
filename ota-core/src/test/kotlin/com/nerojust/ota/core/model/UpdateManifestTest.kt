package com.nerojust.ota.core.model

import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class UpdateManifestTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `decodes snake_case backend json into UpdateManifest`() {
        val body = """
            {
              "version_code": 2,
              "version_name": "1.1.0",
              "apk_url": "https://cdn.example.com/app-v2.apk",
              "checksum": "abc123",
              "file_size": 25000000,
              "changelog": "Bug fixes",
              "force_update": false,
              "rollout_percentage": 100,
              "minimum_version_code": 1,
              "release_date": 1234567890
            }
        """.trimIndent()

        val manifest = json.decodeFromString<UpdateManifest>(body)

        assertEquals(2, manifest.versionCode)
        assertEquals("1.1.0", manifest.versionName)
        assertEquals("https://cdn.example.com/app-v2.apk", manifest.apkUrl)
        assertEquals("abc123", manifest.checksum)
        assertEquals(25000000L, manifest.fileSize)
    }

    @Test
    fun `applies defaults for optional fields`() {
        val body = """
            {
              "version_code": 1,
              "version_name": "1.0.0",
              "apk_url": "https://cdn.example.com/app-v1.apk",
              "checksum": "abc123",
              "file_size": 1000
            }
        """.trimIndent()

        val manifest = json.decodeFromString<UpdateManifest>(body)

        assertEquals(false, manifest.forceUpdate)
        assertEquals(100, manifest.rolloutPercentage)
        assertEquals(1, manifest.minimumVersionCode)
    }
}
