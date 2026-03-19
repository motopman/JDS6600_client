package com.jds6600.sequencer.domain.repository

import com.jds6600.sequencer.domain.model.Sequence
import kotlinx.coroutines.flow.Flow

interface ISequenceRepository {
    fun observeAll(): Flow<List<Sequence>>
    suspend fun getLatest(): Sequence?
    suspend fun getByName(name: String): Sequence?
    suspend fun save(sequence: Sequence)
    suspend fun delete(name: String)
}
