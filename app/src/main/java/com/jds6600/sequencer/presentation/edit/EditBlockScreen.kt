package com.jds6600.sequencer.presentation.edit

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.jds6600.sequencer.domain.model.Waveform
import com.jds6600.sequencer.presentation.components.formatFrequency
import kotlin.math.log10
import kotlin.math.pow

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditBlockScreen(
    onSaved: () -> Unit,
    onCancel: () -> Unit,
    vm: EditBlockViewModel = hiltViewModel()
) {
    val block by vm.block.collectAsState()
    var waveformExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Block") },
                navigationIcon = {
                    IconButton(onClick = { vm.cancel(); onCancel() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))

            // ── Channel ─────────────────────────────────────────────────
            SectionLabel("Channel")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2).forEach { ch ->
                    val selected = block.channel == ch
                    if (selected) {
                        Button(onClick = { vm.setChannel(ch) }) { Text("CH $ch") }
                    } else {
                        TextButton(onClick = { vm.setChannel(ch) }) { Text("CH $ch") }
                    }
                }
            }

            HorizontalDivider()

            // ── Frequency (logarithmic slider) ────────────────────────────
            SectionLabel("Frequency:  ${formatFrequency(block.frequencyHz)}")
            val logMin = log10(0.01)
            val logMax = log10(24_000_000.0)
            val logCurrent = log10(block.frequencyHz).toFloat()
            Slider(
                value = logCurrent,
                onValueChange = { vm.setFrequency(10.0.pow(it.toDouble())) },
                valueRange = logMin.toFloat()..logMax.toFloat(),
                steps = 0
            )
            // Manual text entry
            var freqText by remember(block.frequencyHz) { mutableStateOf(block.frequencyHz.toString()) }
            OutlinedTextField(
                value = freqText,
                onValueChange = {
                    freqText = it
                    it.toDoubleOrNull()?.let { v -> vm.setFrequency(v) }
                },
                label = { Text("Hz") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth()
            )

            HorizontalDivider()

            // ── Amplitude ────────────────────────────────────────────────
            SectionLabel("Amplitude:  ${"%.3f".format(block.amplitudeV)} V")
            Slider(
                value = block.amplitudeV.toFloat(),
                onValueChange = { vm.setAmplitude(it.toDouble()) },
                valueRange = 0f..20f,
                steps = 0
            )

            HorizontalDivider()

            // ── Waveform ─────────────────────────────────────────────────
            SectionLabel("Waveform")
            ExposedDropdownMenuBox(
                expanded = waveformExpanded,
                onExpandedChange = { waveformExpanded = it }
            ) {
                OutlinedTextField(
                    value = block.waveform.label,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(waveformExpanded) },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = waveformExpanded,
                    onDismissRequest = { waveformExpanded = false }
                ) {
                    Waveform.entries.forEach { w ->
                        DropdownMenuItem(
                            text = { Text(w.label) },
                            onClick = { vm.setWaveform(w); waveformExpanded = false }
                        )
                    }
                }
            }

            HorizontalDivider()

            // ── Duration ─────────────────────────────────────────────────
            SectionLabel("Duration:  ${block.durationSeconds} s")
            Slider(
                value = block.durationSeconds.toFloat(),
                onValueChange = { vm.setDuration(it.toInt()) },
                valueRange = 1f..7200f,
                steps = 0
            )

            HorizontalDivider()

            // ── Phase ────────────────────────────────────────────────────
            SectionLabel("Phase:  ${block.phase}°")
            Slider(
                value = block.phase.toFloat(),
                onValueChange = { vm.setPhase(it.toInt()) },
                valueRange = 0f..360f,
                steps = 0
            )

            HorizontalDivider()

            // ── Duty cycle (visible only for PULSE / SQUARE) ─────────────
            if (block.waveform == Waveform.PULSE || block.waveform == Waveform.SQUARE) {
                SectionLabel("Duty Cycle:  ${block.dutyCycle}%")
                Slider(
                    value = block.dutyCycle.toFloat(),
                    onValueChange = { vm.setDutyCycle(it.toInt()) },
                    valueRange = 1f..99f,
                    steps = 0
                )
                HorizontalDivider()
            }

            // ── DC Offset ────────────────────────────────────────────────
            SectionLabel("DC Offset:  ${"%.3f".format(block.offsetV)} V")
            Slider(
                value = block.offsetV.toFloat(),
                onValueChange = { vm.setOffset(it.toDouble()) },
                valueRange = -10f..10f,
                steps = 0
            )

            HorizontalDivider()
            Spacer(Modifier.height(8.dp))

            // ── Action buttons ───────────────────────────────────────────
            Button(
                onClick = { vm.save(); onSaved() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Save") }

            TextButton(
                onClick = { vm.cancel(); onCancel() },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Cancel") }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = androidx.compose.material3.MaterialTheme.typography.labelLarge)
}
