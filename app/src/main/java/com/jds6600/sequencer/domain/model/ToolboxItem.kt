package com.jds6600.sequencer.domain.model

import kotlin.math.log10

/**
 * A palette chip. All 7 default items share the same waveform and amplitude;
 * they differ ONLY in frequency (and thus hue). The user customises details
 * after dropping the block onto the tray.
 */
data class ToolboxItem(
    val id: String,
    val label: String,       // frequency display e.g. "583 680"
    val chakraName: String,  // e.g. "Муладхара"
    val chakraShort: String, // e.g. "Копчик"
    val frequencyHz: Double,
    val hue: Float
) {
    fun toBlock(): SequenceBlock = SequenceBlock(
        frequencyHz     = frequencyHz,
        amplitudeV      = 20.0,
        waveform        = Waveform.SINE,
        durationSeconds = 1800,
        hueOverride     = hue,
        chakraName      = chakraName,
        chakraShort     = chakraShort
    )
}

/** Logarithmic hue: 1 Hz -> 0°, 24 MHz -> 300° */
fun frequencyToHue(hz: Double): Float {
    val minHz = 1.0
    val maxHz = 24_000_000.0
    val clamped = hz.coerceIn(minHz, maxHz)
    val t = (log10(clamped) - log10(minHz)) / (log10(maxHz) - log10(minHz))
    return (t * 300f).toFloat()
}

// Chakra frequencies mapped to rainbow colours
val DEFAULT_TOOLBOX_ITEMS: List<ToolboxItem> = listOf(
    ToolboxItem("muladhara",  "583 680",   "Муладхара",  "Копчик",  583_680.0,     0f),
    ToolboxItem("svadhistana","811 008",   "Свадхистана","Пол",     811_008.0,    30f),
    ToolboxItem("manipura",   "854 016",   "Манипура",   "Пупок",   854_016.0,    60f),
    ToolboxItem("anahata",    "1 081 344", "Анахата",    "Сердце",  1_081_344.0, 120f),
    ToolboxItem("vishuddha",  "654 336",   "Вишудха",    "Горло",   654_336.0,   180f),
    ToolboxItem("ajna",       "758 784",   "Аджна",      "Глаз",    758_784.0,   240f),
    ToolboxItem("sahasrara",  "872 448",   "Сахасрара",  "Венец",   872_448.0,   300f)
)
