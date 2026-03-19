package com.jds6600.sequencer.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Sequence(
    val name: String = "Untitled",
    val blocks: List<SequenceBlock> = emptyList(),
    val loop: Boolean = false,
    val lastModifiedMs: Long = System.currentTimeMillis()
)
