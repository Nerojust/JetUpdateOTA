package com.nerojust.ota.core

import com.nerojust.ota.core.model.UpdateManifest

sealed interface UpdateCheckResult {
    data class Available(val manifest: UpdateManifest) : UpdateCheckResult
    data object UpToDate : UpdateCheckResult
    data object NotInRollout : UpdateCheckResult
    data class Failed(val error: OTAException) : UpdateCheckResult
}
