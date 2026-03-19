package com.jds6600.sequencer.presentation.edit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jds6600.sequencer.domain.model.SequenceBlock
import com.jds6600.sequencer.domain.model.Waveform
import com.jds6600.sequencer.utils.BlockEditingCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditBlockViewModel @Inject constructor(
    private val coordinator: BlockEditingCoordinator
) : ViewModel() {

    private val _block = MutableStateFlow(coordinator.currentBlock() ?: SequenceBlock())
    val block: StateFlow<SequenceBlock> = _block.asStateFlow()

    fun setChannel(ch: Int)             { _block.update { it.copy(channel = ch.coerceIn(1, 2)) } }
    fun setFrequency(hz: Double)        { _block.update { it.copy(frequencyHz = hz.coerceAtLeast(0.01)) } }
    fun setAmplitude(v: Double)         { _block.update { it.copy(amplitudeV = v.coerceIn(0.0, 20.0)) } }
    fun setWaveform(w: Waveform)        { _block.update { it.copy(waveform = w) } }
    fun setDuration(secs: Int)          { _block.update { it.copy(durationSeconds = secs.coerceAtLeast(1)) } }
    fun setPhase(deg: Int)              { _block.update { it.copy(phase = deg.coerceIn(0, 360)) } }
    fun setOffset(v: Double)            { _block.update { it.copy(offsetV = v) } }
    fun setDutyCycle(pct: Int)          { _block.update { it.copy(dutyCycle = pct.coerceIn(1, 99)) } }

    fun save() {
        viewModelScope.launch { coordinator.publishResult(_block.value) }
    }

    fun cancel() { coordinator.cancel() }
}
