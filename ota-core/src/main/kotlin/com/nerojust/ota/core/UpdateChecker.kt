package com.nerojust.ota.core

interface UpdateChecker {
    suspend fun fetchAndEvaluate(currentVersionCode: Int, deviceId: String): UpdateCheckResult
}
