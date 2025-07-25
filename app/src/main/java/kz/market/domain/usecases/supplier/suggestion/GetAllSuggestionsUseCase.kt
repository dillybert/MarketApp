package kz.market.domain.usecases.supplier.suggestion

import kotlinx.coroutines.flow.Flow
import kz.market.domain.models.SupplierSuggestion
import kz.market.domain.repository.SupplierSuggestionRepository
import javax.inject.Inject

class GetAllSuggestionsUseCase @Inject constructor(
    private val suggestionRepository: SupplierSuggestionRepository
) {
    operator fun invoke(): Flow<List<SupplierSuggestion>> = suggestionRepository.getAllSuggestions()
}