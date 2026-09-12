package com.nerojust.ota.core

sealed interface InstallResult {
    data object Success : InstallResult
    data class Failed(val error: OTAException) : InstallResult
}
