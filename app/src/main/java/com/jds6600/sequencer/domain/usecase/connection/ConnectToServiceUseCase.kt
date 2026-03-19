package com.jds6600.sequencer.domain.usecase.connection

import com.jds6600.sequencer.domain.model.ConnectionConfig
import com.jds6600.sequencer.domain.repository.IConnectionRepository
import javax.inject.Inject

class ConnectToServiceUseCase @Inject constructor(
    private val repo: IConnectionRepository
) {
    operator fun invoke(config: ConnectionConfig) = repo.connect(config)
}
