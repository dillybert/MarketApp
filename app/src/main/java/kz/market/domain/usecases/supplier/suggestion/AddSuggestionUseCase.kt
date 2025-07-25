package kz.market.domain.usecases.supplier.suggestion

import kz.market.domain.models.SupplierSuggestion
import kz.market.domain.repository.SupplierSuggestionRepository
import javax.inject.Inject

class AddSuggestionUseCase @Inject constructor(
    private val suggestionRepository: SupplierSuggestionRepository
) {
    suspend operator fun invoke(suggestion: SupplierSuggestion): Result<Unit> = suggestionRepository.addSuggestion(suggestion)
}