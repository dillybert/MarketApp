package kz.market.service.usecase

import kotlinx.coroutines.flow.Flow
import kz.market.service.repository.UpdateRepository
import kz.market.service.utils.UpdateStatus
import java.util.UUID
import javax.inject.Inject

class ObserveDownloadProgressUseCase @Inject constructor(
    private val updateRepository: UpdateRepository,
) {
    operator fun invoke(uuid: UUID): Flow<UpdateStatus> = updateRepository.observeDownload(uuid)
}