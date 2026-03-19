package com.jds6600.sequencer.domain.usecase.sequence

import com.jds6600.sequencer.domain.model.Sequence
import com.jds6600.sequencer.domain.repository.ISequenceRepository
import javax.inject.Inject

class LoadLatestSequenceUseCase @Inject constructor(
    private val repo: ISequenceRepository
) {
    suspend operator fun invoke(): Sequence? = repo.getLatest()
}
