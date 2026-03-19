package com.jds6600.sequencer.domain.model

import kotlinx.serialization.Serializable

@Serializable
enum class Waveform(val label: String) {
    SINE("Sine"),
    SQUARE("Square"),
    TRIANGLE("Triangle"),
    RAMP_UP("Ramp Up"),
    RAMP_DOWN("Ramp Down"),
    PULSE("Pulse"),
    NOISE("Noise"),
    ARBITRARY("Arbitrary")
}
