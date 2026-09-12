package com.nerojust.ota.core

data class UpdateState(
    val lastCheckTimeMillis: Long = 0,
    val pendingVersionCode: Int? = null,
    val downloadedApkPath: String? = null,
)
