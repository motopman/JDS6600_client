package com.jds6600.sequencer.domain.usecase.sequence

import com.jds6600.sequencer.domain.model.Sequence
import com.jds6600.sequencer.domain.repository.ISequenceRepository
import javax.inject.Inject

class SaveSequenceUseCase @Inject constructor(
    private val repo: ISequenceRepository
) {
    suspend operator fun invoke(sequence: Sequence) = repo.save(sequence)
}
