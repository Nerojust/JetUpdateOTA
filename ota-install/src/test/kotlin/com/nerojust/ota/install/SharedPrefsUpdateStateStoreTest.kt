package com.nerojust.ota.install

import com.nerojust.ota.core.UpdateState
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment

@RunWith(RobolectricTestRunner::class)
class SharedPrefsUpdateStateStoreTest {

    private val store = SharedPrefsUpdateStateStore(RuntimeEnvironment.getApplication())

    @Test
    fun `returns default state before anything is stored`() = runTest {
        assertEquals(UpdateState(), store.currentState())
    }

    @Test
    fun `persists and reads back an updated state`() = runTest {
        val state = UpdateState(lastCheckTimeMillis = 111, pendingVersionCode = 5, downloadedApkPath = "/tmp/a.apk")

        store.update(state)

        assertEquals(state, store.currentState())
    }

    @Test
    fun `clear resets to the default state`() = runTest {
        store.update(UpdateState(lastCheckTimeMillis = 111, pendingVersionCode = 5))

        store.clear()

        assertEquals(UpdateState(), store.currentState())
    }
}
