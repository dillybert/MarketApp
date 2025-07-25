package kz.market.domain.usecases.supplier.suggestion

import kz.market.domain.repository.SupplierSuggestionRepository
import javax.inject.Inject

class DeleteSuggestionUseCase @Inject constructor(
    private val suggestionRepository: SupplierSuggestionRepository
) {
    suspend operator fun invoke(id: String): Result<Unit> = suggestionRepository.deleteSuggestion(id)
}