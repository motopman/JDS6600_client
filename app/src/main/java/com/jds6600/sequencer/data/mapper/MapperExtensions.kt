package com.jds6600.sequencer.data.mapper

import com.jds6600.sequencer.data.local.database.entities.SequenceEntity
import com.jds6600.sequencer.domain.model.Sequence
import com.jds6600.sequencer.domain.model.SequenceBlock
import kotlinx.serialization.encodeToString
import com.jds6600.sequencer.domain.model.DEFAULT_TOOLBOX_ITEMS
import kotlinx.serialization.json.Json

private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

fun SequenceEntity.toDomain(): Sequence {
    val blocks = runCatching {
        json.decodeFromString<List<SequenceBlock>>(blocksJson)
            .map { block ->
                // Backfill hue for blocks saved before hueOverride was introduced
                if (block.hueOverride >= 0f) block
                else {
                    val match = DEFAULT_TOOLBOX_ITEMS.minByOrNull {
                        Math.abs(it.frequencyHz - block.frequencyHz)
                    }
                    val hue = if (match != null &&
                        Math.abs(match.frequencyHz - block.frequencyHz) < 1.0)
                        match.hue else -1f
                    block.copy(hueOverride = hue)
                }
            }
    }.getOrDefault(emptyList())
    return Sequence(
        name          = name,
        blocks        = blocks,
        loop          = loop,
        lastModifiedMs = lastModifiedMs
    )
}

fun Sequence.toEntity(): SequenceEntity = SequenceEntity(
    name          = name,
    blocksJson    = json.encodeToString(blocks),
    loop          = loop,
    lastModifiedMs = lastModifiedMs
)
