package com.nerojust.ota.testing

import com.nerojust.ota.core.UpdateCheckResult
import com.nerojust.ota.core.UpdateChecker

class FakeUpdateChecker(private val result: UpdateCheckResult) : UpdateChecker {
    override suspend fun fetchAndEvaluate(
        currentVersionCode: Int,
        deviceId: String,
    ): UpdateCheckResult = result
}
