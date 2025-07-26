package kz.market.service.usecase

import kz.market.service.model.UpdateMetaData
import kz.market.service.repository.UpdateRepository
import javax.inject.Inject

class GetUpdateMetaDataUseCase @Inject constructor(
    private val updateRepository: UpdateRepository
) {
    suspend operator fun invoke(): UpdateMetaData = updateRepository.getUpdateMetaData()
}