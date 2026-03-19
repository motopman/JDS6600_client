package com.jds6600.sequencer.domain.repository

import com.jds6600.sequencer.domain.model.ConnectionConfig
import com.jds6600.sequencer.domain.model.ConnectionState
import com.jds6600.sequencer.data.remote.websocket.WebSocketEvent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface IConnectionRepository {
    val connectionState: StateFlow<ConnectionState>
    val events: Flow<WebSocketEvent>
    /** Millis of last inbound message or successful handshake; 0 if never connected. */
    val lastActivityMs: StateFlow<Long>

    fun connect(config: ConnectionConfig)
    fun disconnect()
    fun sendRaw(message: String)

    /** Persist config so it can be restored next launch. */
    suspend fun saveLastConfig(config: ConnectionConfig)
    /** Returns null if nothing was saved yet. */
    suspend fun loadLastConfig(): ConnectionConfig?
}
