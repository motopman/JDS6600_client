package com.jds6600.sequencer.data.remote.websocket

sealed class WebSocketEvent {
    data object Connected                    : WebSocketEvent()
    data object Disconnected                 : WebSocketEvent()
    data class  MessageReceived(val text: String) : WebSocketEvent()
    data class  Error(val message: String)   : WebSocketEvent()
}
