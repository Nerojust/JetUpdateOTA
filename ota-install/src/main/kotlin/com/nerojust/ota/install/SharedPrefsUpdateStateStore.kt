package com.nerojust.ota.install

import android.content.Context
import com.nerojust.ota.core.UpdateState
import com.nerojust.ota.core.UpdateStateStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val PREFS_NAME = "ota_state"
private const val KEY_LAST_CHECK = "last_check_time_millis"
private const val KEY_PENDING_VERSION = "pending_version_code"
private const val KEY_DOWNLOADED_APK_PATH = "downloaded_apk_path"

class SharedPrefsUpdateStateStore(context: Context) : UpdateStateStore {

    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override suspend fun currentState(): UpdateState = withContext(Dispatchers.IO) {
        UpdateState(
            lastCheckTimeMillis = prefs.getLong(KEY_LAST_CHECK, 0L),
            pendingVersionCode = prefs.getInt(KEY_PENDING_VERSION, 0).takeIf { it != 0 },
            downloadedApkPath = prefs.getString(KEY_DOWNLOADED_APK_PATH, null),
        )
    }

    override suspend fun update(state: UpdateState) = withContext(Dispatchers.IO) {
        prefs.edit()
            .putLong(KEY_LAST_CHECK, state.lastCheckTimeMillis)
            .putInt(KEY_PENDING_VERSION, state.pendingVersionCode ?: 0)
            .putString(KEY_DOWNLOADED_APK_PATH, state.downloadedApkPath)
            .apply()
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        prefs.edit().clear().apply()
    }
}
