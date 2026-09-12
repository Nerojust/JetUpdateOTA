package com.nerojust.ota.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateManifest(
    @SerialName("version_code") val versionCode: Int,
    @SerialName("version_name") val versionName: String,
    @SerialName("apk_url") val apkUrl: String,
    @SerialName("checksum") val checksum: String,
    @SerialName("file_size") val fileSize: Long,
    @SerialName("changelog") val changelog: String? = null,
    @SerialName("force_update") val forceUpdate: Boolean = false,
    @SerialName("rollout_percentage") val rolloutPercentage: Int = 100,
    @SerialName("minimum_version_code") val minimumVersionCode: Int = 1,
    @SerialName("release_date") val releaseDate: Long = 0L,
)
