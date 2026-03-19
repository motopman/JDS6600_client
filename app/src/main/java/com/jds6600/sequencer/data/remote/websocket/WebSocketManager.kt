package com.jds6600.sequencer.data.remote.websocket

import com.jds6600.sequencer.domain.model.ConnectionConfig
import com.jds6600.sequencer.domain.model.ConnectionState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebSocketManager @Inject constructor(
    private val client: OkHttpClient
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val _state = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _state.asStateFlow()

    private val _events = MutableSharedFlow<WebSocketEvent>(replay = 0, extraBufferCapacity = 64)
    val events: SharedFlow<WebSocketEvent> = _events.asSharedFlow()

    private val _lastActivityMs = MutableStateFlow(0L)
    val lastActivityMs: StateFlow<Long> = _lastActivityMs.asStateFlow()

    private var socket: WebSocket? = null
    private val pending = ArrayDeque<String>()
    private var reconnectJob: Job? = null
    private var reconnectDelayMs = BASE_DELAY_MS
    private var currentConfig: ConnectionConfig? = null

    fun connect(config: ConnectionConfig) {
        currentConfig = config
        reconnectDelayMs = BASE_DELAY_MS
        openSocket(config)
    }

    private fun openSocket(config: ConnectionConfig) {
        _state.value = ConnectionState.Connecting
        val url = buildString {
            append("ws://")
            append(config.host)
            append(":")
            append(config.port)
            append("/ws")
            if (config.token.isNotEmpty()) {
                append("?token=")
                append(config.token)
            }
        }
        val request = Request.Builder().url(url).build()
        socket = client.newWebSocket(request, listener)
        Timber.d("WebSocket connecting to %s", url)
    }

    private val listener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            Timber.d("WebSocket connected")
            _state.value = ConnectionState.Connected
            _lastActivityMs.value = System.currentTimeMillis()
            reconnectDelayMs = BASE_DELAY_MS
            flushPending()
            scope.launch { _events.emit(WebSocketEvent.Connected) }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            _lastActivityMs.value = System.currentTimeMillis()
            Timber.d("WS <-- %s", text)
            scope.launch { _events.emit(WebSocketEvent.MessageReceived(text)) }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            Timber.d("WebSocket closing: %d %s", code, reason)
            webSocket.close(1000, null)
            _state.value = ConnectionState.Disconnected
            scope.launch { _events.emit(WebSocketEvent.Disconnected) }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            Timber.w(t, "WebSocket failure")
            _state.value = ConnectionState.Error(t.message ?: "Unknown error")
            scope.launch { _events.emit(WebSocketEvent.Error(t.message ?: "Unknown")) }
            scheduleReconnect()
        }
    }

    fun sendRaw(message: String) {
        if (_state.value is ConnectionState.Connected) {
            socket?.send(message)
            Timber.d("WS --> %s", message)
        } else {
            pending.addLast(message)
            Timber.d("WS queued (offline): %s", message)
        }
    }

    private fun flushPending() {
        while (pending.isNotEmpty()) {
            val msg = pending.removeFirst()
            socket?.send(msg)
            Timber.d("WS flushed: %s", msg)
        }
    }

    private fun scheduleReconnect() {
        val cfg = currentConfig ?: return
        reconnectJob?.cancel()
        reconnectJob = scope.launch {
            Timber.d("Reconnect in %d ms", reconnectDelayMs)
            delay(reconnectDelayMs)
            reconnectDelayMs = (reconnectDelayMs * 2).coerceAtMost(MAX_DELAY_MS)
            openSocket(cfg)
        }
    }

    fun disconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
        currentConfig = null
        socket?.close(1000, "User disconnected")
        socket = null
        pending.clear()
        _state.value = ConnectionState.Disconnected
    }

    companion object {
        private const val BASE_DELAY_MS = 2_000L
        private const val MAX_DELAY_MS  = 30_000L
    }
}
