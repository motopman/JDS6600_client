package com.jds6600.sequencer.data.local.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.jds6600.sequencer.data.local.database.entities.SequenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SequenceDao {
    @Query("SELECT * FROM sequences ORDER BY lastModifiedMs DESC")
    fun observeAll(): Flow<List<SequenceEntity>>

    @Query("SELECT * FROM sequences ORDER BY lastModifiedMs DESC LIMIT 1")
    suspend fun getLatest(): SequenceEntity?

    @Query("SELECT * FROM sequences WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): SequenceEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SequenceEntity)

    @Query("DELETE FROM sequences WHERE name = :name")
    suspend fun deleteByName(name: String)
}
