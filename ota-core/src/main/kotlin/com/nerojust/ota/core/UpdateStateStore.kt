package com.nerojust.ota.core

interface UpdateStateStore {
    suspend fun currentState(): UpdateState

    suspend fun update(state: UpdateState)

    suspend fun clear()
}
