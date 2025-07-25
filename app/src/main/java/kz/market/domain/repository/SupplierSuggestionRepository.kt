package kz.market.domain.repository

import kotlinx.coroutines.flow.Flow
import kz.market.domain.models.SupplierSuggestion

interface SupplierSuggestionRepository {
    fun getAllSuggestions(): Flow<List<SupplierSuggestion>>
    suspend fun addSuggestion(suggestion: SupplierSuggestion): Result<Unit>
    suspend fun updateSuggestion(suggestion: SupplierSuggestion): Result<Unit>
    suspend fun deleteSuggestion(id: String): Result<Unit>
}