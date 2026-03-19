package com.jds6600.sequencer.domain.usecase.connection

import com.jds6600.sequencer.domain.model.Sequence
import com.jds6600.sequencer.domain.model.SequenceBlock
import com.jds6600.sequencer.domain.repository.IConnectionRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject

class SendSequenceUseCase @Inject constructor(
    private val repo: IConnectionRepository
) {
    private val json = Json { encodeDefaults = true }

    fun uploadSequence(sequence: Sequence) {
        val payload = json.encodeToString(sequence)
        repo.sendRaw(buildString {
            append("{")
            append("\"type\":\"sequence_upload\",")
            append("\"payload\":")
            append(payload)
            append("}")
        })
    }

    fun start()  = repo.sendRaw("{\"type\":\"sequence_start\"}")
    fun stop() {
        repo.sendRaw("{\"type\":\"sequence_stop\",\"channel\":1}")
        repo.sendRaw("{\"type\":\"sequence_stop\",\"channel\":2}")
    }

    fun previewBlock(block: SequenceBlock) {
        val payload = json.encodeToString(block)
        repo.sendRaw(buildString {
            append("{")
            append("\"type\":\"preview_block\",")
            append("\"payload\":")
            append(payload)
            append("}")
        })
    }
}
