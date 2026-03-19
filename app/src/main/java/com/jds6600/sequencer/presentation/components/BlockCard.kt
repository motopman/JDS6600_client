package com.jds6600.sequencer.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.jds6600.sequencer.domain.model.SequenceBlock
import com.jds6600.sequencer.domain.model.Waveform
import com.jds6600.sequencer.utils.blockColor
import kotlin.math.sin
import kotlin.random.Random

@Composable
fun BlockCard(
    block: SequenceBlock,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val accentColor = blockColor(block.frequencyHz, block.hueOverride)
    val onAccent = if (accentColor.luminance() > 0.4f) Color(0xFF1A1A1A) else Color.White

    Card(
        modifier  = modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable(onClick = onClick),
        shape     = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors    = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {

            // ── Left coloured panel: chakra name + frequency ───────────────
            Box(
                modifier = Modifier
                    .width(68.dp)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(topStart = 12.dp, bottomStart = 12.dp))
                    .background(accentColor),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(horizontal = 4.dp)
                ) {
                    if (block.chakraName.isNotEmpty()) {
                        Text(
                            text       = block.chakraName,
                            color      = onAccent,
                            fontSize   = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign  = TextAlign.Center,
                            lineHeight = 10.sp,
                            maxLines   = 1
                        )
                        Text(
                            text      = block.chakraShort,
                            color     = onAccent.copy(alpha = 0.8f),
                            fontSize  = 8.sp,
                            textAlign = TextAlign.Center,
                            maxLines  = 1
                        )
                    } else {
                        Text(
                            text       = formatFreqShort(block.frequencyHz),
                            color      = onAccent,
                            fontSize   = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign  = TextAlign.Center
                        )
                        Text(
                            text      = formatFreqUnit(block.frequencyHz),
                            color     = onAccent.copy(alpha = 0.75f),
                            fontSize  = 10.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // ── Waveform canvas ────────────────────────────────────────────
            WaveformCanvas(
                waveform = block.waveform,
                color    = accentColor,
                modifier = Modifier
                    .size(width = 56.dp, height = 36.dp)
                    .padding(horizontal = 4.dp)
            )

            // ── Details ────────────────────────────────────────────────────
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 4.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text  = "${block.amplitudeV} V",
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold)
                )
                Text(
                    text  = "ch${block.channel}  ·  ${formatDuration(block.durationSeconds)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete block")
            }
        }
    }
}

// ── Waveform shapes drawn with Canvas ─────────────────────────────────────────

@Composable
fun WaveformCanvas(
    waveform: Waveform,
    color: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val mid = h / 2f
        val amp = h * 0.38f
        val stroke = Stroke(width = 2.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
        val path = Path()

        when (waveform) {
            Waveform.SINE -> {
                val steps = 128
                path.moveTo(0f, mid)
                for (i in 1..steps) {
                    val x = w * i / steps
                    val y = mid - amp * sin(2 * Math.PI * i / steps).toFloat()
                    path.lineTo(x, y)
                }
            }

            Waveform.SQUARE -> {
                path.moveTo(0f, mid - amp)
                path.lineTo(w * 0.5f, mid - amp)
                path.lineTo(w * 0.5f, mid + amp)
                path.lineTo(w, mid + amp)
            }

            Waveform.TRIANGLE -> {
                path.moveTo(0f, mid)
                path.lineTo(w * 0.25f, mid - amp)
                path.lineTo(w * 0.75f, mid + amp)
                path.lineTo(w, mid)
            }

            Waveform.RAMP_UP -> {
                path.moveTo(0f, mid + amp)
                path.lineTo(w * 0.48f, mid - amp)
                path.moveTo(w * 0.48f, mid - amp)
                path.lineTo(w * 0.5f, mid + amp)
                path.lineTo(w, mid - amp)
            }

            Waveform.RAMP_DOWN -> {
                path.moveTo(0f, mid - amp)
                path.lineTo(w * 0.48f, mid + amp)
                path.moveTo(w * 0.48f, mid + amp)
                path.lineTo(w * 0.5f, mid - amp)
                path.lineTo(w, mid + amp)
            }

            Waveform.PULSE -> {
                // Narrow positive pulse in the centre
                path.moveTo(0f, mid + amp * 0.3f)
                path.lineTo(w * 0.35f, mid + amp * 0.3f)
                path.lineTo(w * 0.35f, mid - amp)
                path.lineTo(w * 0.5f,  mid - amp)
                path.lineTo(w * 0.5f,  mid + amp * 0.3f)
                path.lineTo(w, mid + amp * 0.3f)
            }

            Waveform.NOISE -> {
                val rng = Random(42)
                path.moveTo(0f, mid)
                var x = 0f
                val step = w / 14f
                while (x <= w) {
                    val y = mid + amp * (rng.nextFloat() * 2f - 1f)
                    path.lineTo(x, y)
                    x += step
                }
            }

            Waveform.ARBITRARY -> {
                // Multi-freq looking squiggle
                val steps = 128
                path.moveTo(0f, mid)
                for (i in 1..steps) {
                    val t = i.toFloat() / steps
                    val x = w * t
                    val y = mid - amp * (
                        0.6f * sin(2 * Math.PI * t * 2).toFloat() +
                        0.3f * sin(2 * Math.PI * t * 5 + 1.0).toFloat() +
                        0.1f * sin(2 * Math.PI * t * 11).toFloat()
                    )
                    path.lineTo(x, y)
                }
            }
        }

        drawPath(path, color = color, style = stroke)
    }
}

// ── Chip preview (used in drag ghost) ────────────────────────────────────────

@Composable
fun MiniBlockChip(color: Color, label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(label, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
    }
}

// ── Formatters ─────────────────────────────────────────────────────────────────

fun formatFrequency(hz: Double): String = when {
    hz >= 1_000_000.0 -> "${"%.2f".format(hz / 1_000_000.0)} MHz"
    hz >= 1_000.0     -> "${"%.2f".format(hz / 1_000.0)} kHz"
    else              -> "${"%.2f".format(hz)} Hz"
}

private fun formatFreqShort(hz: Double): String = when {
    hz >= 1_000_000.0 -> "%.2f".format(hz / 1_000_000.0)
    hz >= 1_000.0     -> "%.0f".format(hz / 1_000.0)
    else              -> "%.0f".format(hz)
}

private fun formatFreqUnit(hz: Double): String = when {
    hz >= 1_000_000.0 -> "MHz"
    hz >= 1_000.0     -> "kHz"
    else              -> "Hz"
}

fun formatDuration(seconds: Int): String = when {
    seconds >= 3600 -> "${seconds / 3600}h ${(seconds % 3600) / 60}m"
    seconds >= 60   -> "${seconds / 60}m ${seconds % 60}s"
    else            -> "${seconds}s"
}
