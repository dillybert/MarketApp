package kz.market.service.usecase

import kz.market.service.model.UpdateMetaData
import kz.market.service.repository.UpdateRepository
import java.util.UUID
import javax.inject.Inject

class GetDownloadWorkRequestUUIDUseCase @Inject constructor(
    private val updateRepository: UpdateRepository
) {
    operator fun invoke(updateMetaData: UpdateMetaData): UUID = updateRepository.getDownloadWorkRequestUUID(updateMetaData)
}