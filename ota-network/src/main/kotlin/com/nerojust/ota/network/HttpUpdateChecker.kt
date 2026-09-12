package com.nerojust.ota.network

import com.nerojust.ota.core.OTAException
import com.nerojust.ota.core.UpdateCheckResult
import com.nerojust.ota.core.UpdateChecker
import com.nerojust.ota.core.model.UpdateManifest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

private val json = Json { ignoreUnknownKeys = true }

@Serializable
private data class ManifestEnvelope(
    @SerialName("has_update") val hasUpdate: Boolean,
    @SerialName("reason") val reason: String? = null,
    @SerialName("manifest") val manifest: UpdateManifest? = null,
)

class HttpUpdateChecker(
    private val httpClient: OkHttpClient,
    private val manifestUrl: String,
) : UpdateChecker {

    override suspend fun fetchAndEvaluate(currentVersionCode: Int, deviceId: String): UpdateCheckResult =
        withContext(Dispatchers.IO) {
            try {
                val url = manifestUrl.toHttpUrl().newBuilder()
                    .addQueryParameter("app_version", currentVersionCode.toString())
                    .addQueryParameter("device_id", deviceId)
                    .build()
                val request = Request.Builder().url(url).build()
                httpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) {
                        return@withContext UpdateCheckResult.Failed(
                            OTAException.NetworkError(IOException("HTTP ${response.code}"))
                        )
                    }
                    val body = response.body?.string()
                        ?: return@withContext UpdateCheckResult.Failed(
                            OTAException.NetworkError(IOException("Empty response body"))
                        )
                    val envelope = json.decodeFromString<ManifestEnvelope>(body)
                    when {
                        envelope.hasUpdate && envelope.manifest != null -> UpdateCheckResult.Available(envelope.manifest)
                        envelope.reason == "not_in_rollout" -> UpdateCheckResult.NotInRollout
                        else -> UpdateCheckResult.UpToDate
                    }
                }
            } catch (e: IOException) {
                UpdateCheckResult.Failed(OTAException.NetworkError(e))
            } catch (e: SerializationException) {
                UpdateCheckResult.Failed(OTAException.NetworkError(e))
            }
        }
}

fun defaultUpdateChecker(httpClient: OkHttpClient, manifestUrl: String): UpdateChecker =
    HttpUpdateChecker(httpClient, manifestUrl)
