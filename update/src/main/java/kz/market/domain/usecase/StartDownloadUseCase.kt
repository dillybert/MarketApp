package kz.market.domain.usecase

import kz.market.domain.model.UpdateMetaData
import kz.market.domain.repository.UpdateRepository
import java.util.UUID
import javax.inject.Inject

class StartDownloadUseCase @Inject constructor(
    private val updateRepository: UpdateRepository
) {
    operator fun invoke(updateMetaData: UpdateMetaData): UUID = updateRepository.enqueueDownload(updateMetaData)
}