package com.jds6600.sequencer.domain.usecase.sequence

import com.jds6600.sequencer.domain.model.Sequence
import com.jds6600.sequencer.domain.repository.ISequenceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllSequencesUseCase @Inject constructor(
    private val repo: ISequenceRepository
) {
    operator fun invoke(): Flow<List<Sequence>> = repo.observeAll()
}
