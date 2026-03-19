package com.jds6600.sequencer.domain.model

import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class SequenceBlock(
    val id: String = UUID.randomUUID().toString(),
    val channel: Int = 1,
    val frequencyHz: Double = 1000.0,
    val amplitudeV: Double = 20.0,
    val waveform: Waveform = Waveform.SINE,
    val durationSeconds: Int = 1800,    // 30 minutes
    val phase: Int = 0,
    val offsetV: Double = 0.0,
    val dutyCycle: Int = 50,
    /** Explicit hue (0-300°) set when block is created from a toolbox item.
     *  -1 means "derive from frequency" (fallback for manually entered blocks). */
    val hueOverride: Float = -1f,
    val chakraName: String = "",
    val chakraShort: String = ""
)
