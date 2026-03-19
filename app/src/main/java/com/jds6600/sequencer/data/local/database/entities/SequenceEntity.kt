package com.jds6600.sequencer.data.local.database.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sequences")
data class SequenceEntity(
    @PrimaryKey val name: String,
    val blocksJson: String,
    val loop: Boolean = false,
    val lastModifiedMs: Long
)
