package com.jds6600.sequencer.data.repository

import com.jds6600.sequencer.data.local.preferences.ConnectionPreferences
import com.jds6600.sequencer.data.remote.websocket.WebSocketEvent
import com.jds6600.sequencer.data.remote.websocket.WebSocketManager
import com.jds6600.sequencer.domain.model.ConnectionConfig
import com.jds6600.sequencer.domain.model.ConnectionState
import com.jds6600.sequencer.domain.repository.IConnectionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConnectionRepositoryImpl @Inject constructor(
    private val manager: WebSocketManager,
    private val prefs: ConnectionPreferences
) : IConnectionRepository {

    override val connectionState: StateFlow<ConnectionState> = manager.connectionState
    override val events: Flow<WebSocketEvent>                = manager.events
    override val lastActivityMs: StateFlow<Long>             = manager.lastActivityMs

    override fun connect(config: ConnectionConfig) = manager.connect(config)
    override fun disconnect()                       = manager.disconnect()
    override fun sendRaw(message: String)           = manager.sendRaw(message)

    override suspend fun saveLastConfig(config: ConnectionConfig) = prefs.save(config)
    override suspend fun loadLastConfig(): ConnectionConfig?      = prefs.load()
}
