package kz.market.domain.usecase

import kotlinx.coroutines.flow.Flow
import kz.market.domain.UpdateStatus
import kz.market.domain.repository.UpdateRepository
import java.util.UUID
import javax.inject.Inject

class ObserveDownloadUseCase @Inject constructor(
    private val updateRepository: UpdateRepository
) {
    operator fun invoke(uuid: UUID): Flow<UpdateStatus> = updateRepository.observeDownloadProgress(uuid)
}