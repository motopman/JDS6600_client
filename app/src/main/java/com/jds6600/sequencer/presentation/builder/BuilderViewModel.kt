package com.jds6600.sequencer.presentation.builder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.jds6600.sequencer.domain.model.ConnectionState
import com.jds6600.sequencer.domain.model.Sequence
import com.jds6600.sequencer.domain.model.SequenceBlock
import com.jds6600.sequencer.domain.model.ToolboxItem
import com.jds6600.sequencer.domain.repository.IConnectionRepository
import com.jds6600.sequencer.domain.usecase.connection.SendSequenceUseCase
import com.jds6600.sequencer.domain.usecase.sequence.LoadLatestSequenceUseCase
import com.jds6600.sequencer.domain.usecase.sequence.SaveSequenceUseCase
import com.jds6600.sequencer.utils.BlockEditingCoordinator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class BuilderViewModel @Inject constructor(
    private val loadLatest: LoadLatestSequenceUseCase,
    private val save: SaveSequenceUseCase,
    private val sendSequence: SendSequenceUseCase,
    private val coordinator: BlockEditingCoordinator,
    connectionRepository: IConnectionRepository
) : ViewModel() {

    private val _sequence = MutableStateFlow(Sequence())
    val sequence: StateFlow<Sequence> = _sequence.asStateFlow()

    val connectionState: StateFlow<ConnectionState> = connectionRepository.connectionState
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ConnectionState.Disconnected)

    /** Millis of last server message — used to show 'last contact' in UI. */
    val lastActivityMs: StateFlow<Long> = connectionRepository.lastActivityMs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0L)

    init {
        viewModelScope.launch {
            val latest = loadLatest() ?: Sequence(name = "My Sequence")
            _sequence.value = latest
        }
        // Observe edit results from EditBlockScreen
        viewModelScope.launch {
            coordinator.result.collect { updated -> updateBlock(updated) }
        }
    }

    fun addBlockAt(item: ToolboxItem, index: Int) {
        val block = item.toBlock().copy(id = UUID.randomUUID().toString())
        _sequence.update { seq ->
            val list = seq.blocks.toMutableList()
            val safeIdx = index.coerceIn(0, list.size)
            list.add(safeIdx, block)
            seq.copy(blocks = list, lastModifiedMs = System.currentTimeMillis())
        }
        persistAsync()
    }

    fun removeBlock(id: String) {
        _sequence.update { seq ->
            seq.copy(blocks = seq.blocks.filter { it.id != id },
                     lastModifiedMs = System.currentTimeMillis())
        }
        persistAsync()
    }

    fun moveBlock(from: Int, to: Int) {
        if (from == to) return
        _sequence.update { seq ->
            val list = seq.blocks.toMutableList()
            val block = list.removeAt(from)
            list.add(to.coerceIn(0, list.size), block)
            seq.copy(blocks = list, lastModifiedMs = System.currentTimeMillis())
        }
        persistAsync()
    }

    fun startEditBlock(block: SequenceBlock) {
        coordinator.startEditing(block)
    }

    private fun updateBlock(updated: SequenceBlock) {
        _sequence.update { seq ->
            seq.copy(
                blocks = seq.blocks.map { if (it.id == updated.id) updated else it },
                lastModifiedMs = System.currentTimeMillis()
            )
        }
        persistAsync()
    }

    fun playSequence() {
        val seq = _sequence.value
        if (seq.blocks.isEmpty()) return
        // stop any running sequence, then upload — server auto-starts on SequenceLoaded
        sendSequence.stop()
        sendSequence.uploadSequence(seq)
        persistAsync()
    }

    fun stopSequence() = sendSequence.stop()

    fun renameSequence(name: String) {
        _sequence.update { it.copy(name = name, lastModifiedMs = System.currentTimeMillis()) }
        persistAsync()
    }

    private fun persistAsync() {
        viewModelScope.launch { save(_sequence.value) }
    }
}
