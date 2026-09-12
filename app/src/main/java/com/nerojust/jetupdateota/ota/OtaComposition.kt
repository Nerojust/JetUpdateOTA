package com.nerojust.jetupdateota.ota

import android.content.Context
import com.nerojust.ota.core.OTAConfig
import com.nerojust.ota.core.OTAManager
import com.nerojust.ota.download.defaultUpdateDownloader
import com.nerojust.ota.install.defaultUpdateInstaller
import com.nerojust.ota.install.defaultUpdateStateStore
import com.nerojust.ota.network.defaultUpdateChecker
import okhttp3.OkHttpClient

fun createOtaManager(context: Context, manifestUrl: String): OTAManager {
    val config = OTAConfig(manifestUrl = manifestUrl)
    val httpClient = OkHttpClient()
    return OTAManager(
        config = config,
        checker = defaultUpdateChecker(httpClient, config.manifestUrl),
        downloader = defaultUpdateDownloader(httpClient),
        installer = defaultUpdateInstaller(context),
        stateStore = defaultUpdateStateStore(context),
    )
}
