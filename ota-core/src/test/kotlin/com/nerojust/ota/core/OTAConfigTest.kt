package com.nerojust.ota.core

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test

class OTAConfigTest {

    @Test
    fun `accepts an https manifest url`() {
        val config = OTAConfig(manifestUrl = "https://example.com/api/update-manifest")

        assertEquals("https://example.com/api/update-manifest", config.manifestUrl)
    }

    @Test
    fun `rejects a non-https manifest url`() {
        assertThrows(IllegalArgumentException::class.java) {
            OTAConfig(manifestUrl = "http://example.com/api/update-manifest")
        }
    }
}
