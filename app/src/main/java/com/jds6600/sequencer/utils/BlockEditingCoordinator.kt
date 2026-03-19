package com.jds6600.sequencer.utils

import com.jds6600.sequencer.domain.model.SequenceBlock
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Mediates block data between BuilderViewModel and EditBlockViewModel
 * without serialising through Navigation arguments.
 */
@Singleton
class BlockEditingCoordinator @Inject constructor() {

    private val _current = MutableStateFlow<SequenceBlock?>(null)
    val current: StateFlow<SequenceBlock?> = _current.asStateFlow()

    private val _result = MutableSharedFlow<SequenceBlock>(replay = 0, extraBufferCapacity = 1)
    val result: SharedFlow<SequenceBlock> = _result.asSharedFlow()

    fun startEditing(block: SequenceBlock) {
        _current.value = block
    }

    fun currentBlock(): SequenceBlock? = _current.value

    suspend fun publishResult(block: SequenceBlock) {
        _current.value = null
        _result.emit(block)
    }

    fun cancel() {
        _current.value = null
    }
}
