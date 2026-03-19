package com.jds6600.sequencer.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.jds6600.sequencer.data.local.database.entities.SequenceEntity

@Database(
    entities = [SequenceEntity::class],
    version  = 3,
    exportSchema = false
)
abstract class SequenceDatabase : RoomDatabase() {
    abstract fun sequenceDao(): SequenceDao
}
