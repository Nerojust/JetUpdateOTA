package com.nerojust.ota.core

data class OTAConfig(
    val manifestUrl: String,
) {
    init {
        require(manifestUrl.startsWith("https://")) {
            "manifestUrl must use HTTPS for security"
        }
    }
}
