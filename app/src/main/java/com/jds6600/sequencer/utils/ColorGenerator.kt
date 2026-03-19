package com.jds6600.sequencer.utils

import androidx.compose.ui.graphics.Color
import com.jds6600.sequencer.domain.model.frequencyToHue
import kotlin.math.abs

/**
 * Maps a frequency (Hz) to a vivid HSL colour.
 * Only frequency drives the hue; saturation and lightness are fixed.
 */
fun frequencyToColor(frequencyHz: Double): Color {
    val hue = frequencyToHue(frequencyHz)
    return hslToColor(hue, saturation = 0.85f, lightness = 0.55f)
}

/** Use explicit hue if set (≥ 0), otherwise derive from frequency. */
fun blockColor(frequencyHz: Double, hueOverride: Float): Color =
    if (hueOverride >= 0f)
        hslToColor(hueOverride, saturation = 0.85f, lightness = 0.50f)
    else
        frequencyToColor(frequencyHz)

fun hueToColor(hue: Float): Color =
    hslToColor(hue, saturation = 0.85f, lightness = 0.55f)

/**
 * Converts HSL (hue 0-360, saturation 0-1, lightness 0-1) to Compose Color.
 */
fun hslToColor(hue: Float, saturation: Float, lightness: Float): Color {
    val h = hue / 360f
    val s = saturation
    val l = lightness
    val q = if (l < 0.5f) l * (1f + s) else l + s - l * s
    val p = 2f * l - q
    val r = hue2rgb(p, q, h + 1f / 3f)
    val g = hue2rgb(p, q, h)
    val b = hue2rgb(p, q, h - 1f / 3f)
    return Color(r, g, b)
}

private fun hue2rgb(p: Float, q: Float, t_: Float): Float {
    var t = t_
    if (t < 0f) t += 1f
    if (t > 1f) t -= 1f
    return when {
        t < 1f / 6f -> p + (q - p) * 6f * t
        t < 1f / 2f -> q
        t < 2f / 3f -> p + (q - p) * (2f / 3f - t) * 6f
        else        -> p
    }
}
