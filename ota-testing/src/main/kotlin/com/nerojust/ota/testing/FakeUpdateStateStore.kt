package com.nerojust.ota.testing

import com.nerojust.ota.core.UpdateState
import com.nerojust.ota.core.UpdateStateStore

class FakeUpdateStateStore(initial: UpdateState = UpdateState()) : UpdateStateStore {
    private var state = initial

    override suspend fun currentState(): UpdateState = state

    override suspend fun update(state: UpdateState) {
        this.state = state
    }

    override suspend fun clear() {
        state = UpdateState()
    }
}
