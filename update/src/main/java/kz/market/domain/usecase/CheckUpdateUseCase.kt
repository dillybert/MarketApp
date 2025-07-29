package kz.market.domain.usecase

import kz.market.domain.model.UpdateMetaData
import kz.market.domain.repository.UpdateRepository
import javax.inject.Inject

class CheckUpdateUseCase @Inject constructor(
    private val updateRepository: UpdateRepository
) {
    suspend operator fun invoke(): UpdateMetaData = updateRepository.fetchUpdateMetaData()
}