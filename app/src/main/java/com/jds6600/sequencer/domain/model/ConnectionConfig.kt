package com.jds6600.sequencer.domain.model

data class ConnectionConfig(
    val host: String,
    val port: Int = 8080,
    val token: String = ""
)
