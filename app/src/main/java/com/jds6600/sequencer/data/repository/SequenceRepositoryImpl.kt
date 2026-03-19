package com.jds6600.sequencer.data.repository

import com.jds6600.sequencer.data.local.database.SequenceDao
import com.jds6600.sequencer.data.mapper.toDomain
import com.jds6600.sequencer.data.mapper.toEntity
import com.jds6600.sequencer.domain.model.Sequence
import com.jds6600.sequencer.domain.repository.ISequenceRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SequenceRepositoryImpl @Inject constructor(
    private val dao: SequenceDao
) : ISequenceRepository {

    override fun observeAll(): Flow<List<Sequence>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun getLatest(): Sequence? =
        dao.getLatest()?.toDomain()

    override suspend fun getByName(name: String): Sequence? =
        dao.getByName(name)?.toDomain()

    override suspend fun save(sequence: Sequence) =
        dao.upsert(sequence.toEntity())

    override suspend fun delete(name: String) =
        dao.deleteByName(name)
}
