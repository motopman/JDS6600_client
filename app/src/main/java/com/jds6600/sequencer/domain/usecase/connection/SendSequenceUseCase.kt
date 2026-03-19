package com.jds6600.sequencer.domain.usecase.connection

import com.jds6600.sequencer.domain.model.Sequence
import com.jds6600.sequencer.domain.model.SequenceBlock
import com.jds6600.sequencer.domain.repository.IConnectionRepository
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import javax.inject.Inject

class SendSequenceUseCase @Inject constructor(
    private val repo: IConnectionRepository
) {
    private val json = Json { encodeDefaults = true }

    /**
     * Upload sequence. Server auto-starts on SequenceLoaded — no separate start() needed.
     * Only sends fields the Rust server actually knows about; UI-only fields are stripped.
     */
    fun uploadSequence(sequence: Sequence) {
        val blocksArray = buildString {
            append("[")
            sequence.blocks.forEachIndexed { i, b ->
                if (i > 0) append(",")
                append(blockToJson(b))
            }
            append("]")
        }
        val payload = buildString {
            append("{")
            append("\"name\":\"${sequence.name}\",")
            append("\"blocks\":$blocksArray,")
            append("\"loop\":${sequence.loop}")
            append("}")
        }
        repo.sendRaw("""{"type":"sequence_upload","payload":$payload}""")
    }

    /** {"type":"control","payload":{"command":"stop"}} */
    fun stop() = repo.sendRaw("""{"type":"control","payload":{"command":"stop"}}""")

    /** {"type":"control","payload":{"command":"start"}} — normally not needed (server auto-starts) */
    fun start() = repo.sendRaw("""{"type":"control","payload":{"command":"start"}}""")

    fun pause()  = repo.sendRaw("""{"type":"control","payload":{"command":"pause"}}""")
    fun resume() = repo.sendRaw("""{"type":"control","payload":{"command":"resume"}}""")

    fun previewBlock(block: SequenceBlock) {
        repo.sendRaw("""{"type":"quick_command","payload":{"action":"preview","block":${blockToJson(block)}}}""")
    }

    /** Serialize only server-relevant fields — no hueOverride/chakraName/chakraShort */
    private fun blockToJson(b: SequenceBlock): String = buildString {
        append("{")
        append("\"id\":\"${b.id}\",")
        append("\"channel\":${b.channel},")
        append("\"frequencyHz\":${b.frequencyHz},")
        append("\"amplitudeV\":${b.amplitudeV},")
        append("\"waveform\":\"${b.waveform.name.lowercase()}\",")
        append("\"durationSeconds\":${b.durationSeconds},")
        append("\"phase\":${b.phase},")
        append("\"offsetV\":${b.offsetV},")
        append("\"dutyCycle\":${b.dutyCycle}")
        append("}")
    }
}
