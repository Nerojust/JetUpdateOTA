package com.nerojust.ota.core

sealed class OTAException(message: String, cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(cause: Throwable) : OTAException("Network request failed", cause)
    class ChecksumMismatch : OTAException("Downloaded file checksum does not match manifest")
    class SignatureInvalid : OTAException("APK signature does not match installed app")
    class InstallFailed(cause: Throwable? = null) : OTAException("Installation failed", cause)
}
