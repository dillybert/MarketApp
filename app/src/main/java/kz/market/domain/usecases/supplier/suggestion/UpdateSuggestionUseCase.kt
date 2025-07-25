package kz.market.domain.usecases.supplier.suggestion

import kz.market.domain.models.SupplierSuggestion
import kz.market.domain.repository.SupplierSuggestionRepository
import javax.inject.Inject

class UpdateSuggestionUseCase @Inject constructor(
    private val suggestionRepository: SupplierSuggestionRepository
) {
    suspend operator fun invoke(suggestion: SupplierSuggestion): Result<Unit> = suggestionRepository.updateSuggestion(suggestion)
}