package com.jds6600.sequencer.presentation.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jds6600.sequencer.domain.model.ConnectionConfig
import com.jds6600.sequencer.domain.model.ConnectionState
import com.jds6600.sequencer.domain.repository.IConnectionRepository
import com.jds6600.sequencer.domain.usecase.connection.ConnectToServiceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val connectUseCase: ConnectToServiceUseCase,
    private val connectionRepository: IConnectionRepository
) : ViewModel() {

    val host  = MutableStateFlow("")
    val port  = MutableStateFlow("8080")
    val token = MutableStateFlow("")

    val connectionState: StateFlow<ConnectionState> = connectionRepository.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConnectionState.Disconnected)

    /** Millis since epoch of last inbound data; 0 = never. Drives "last contact" indicator. */
    val lastActivityMs: StateFlow<Long> = connectionRepository.lastActivityMs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    init {
        // On start: load saved config, pre-fill fields, attempt reconnect
        viewModelScope.launch {
            val saved = connectionRepository.loadLastConfig()
            if (saved != null) {
                host.value  = saved.host
                port.value  = saved.port.toString()
                token.value = saved.token
                connectUseCase(saved)   // silent auto-reconnect
            }
        }

        // Whenever we reach Connected state, persist the config that worked
        viewModelScope.launch {
            connectionRepository.connectionState.collect { state ->
                if (state is ConnectionState.Connected) {
                    val cfg = ConnectionConfig(
                        host  = host.value.trim(),
                        port  = port.value.toIntOrNull() ?: 8080,
                        token = token.value.trim()
                    )
                    connectionRepository.saveLastConfig(cfg)
                }
            }
        }
    }

    fun applyQrResult(url: String) {
        // Supports: ws://host:port/ws  and  ws://host:port/ws?token=abc
        val hostPortRegex = Regex("""ws://([^:/]+):(\d+)""")
        val tokenRegex    = Regex("""[?&]token=([\w-]+)""")
        val hpMatch = hostPortRegex.find(url) ?: return
        host.value  = hpMatch.groupValues[1]
        port.value  = hpMatch.groupValues[2]
        token.value = tokenRegex.find(url)?.groupValues?.get(1) ?: ""
    }

    fun connect() {
        val cfg = ConnectionConfig(
            host  = host.value.trim(),
            port  = port.value.toIntOrNull() ?: 8080,
            token = token.value.trim()
        )
        connectUseCase(cfg)
    }

    fun connectOffline() { /* proceed to builder without service */ }
}
