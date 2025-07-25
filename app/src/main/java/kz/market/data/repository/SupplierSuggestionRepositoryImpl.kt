package kz.market.data.repository

import kotlinx.coroutines.flow.Flow
import kz.market.data.remote.FirebaseFirestoreService
import kz.market.domain.models.SupplierSuggestion
import kz.market.domain.repository.SupplierSuggestionRepository
import javax.inject.Inject

class SupplierSuggestionRepositoryImpl @Inject constructor(
    private val service: FirebaseFirestoreService
) : SupplierSuggestionRepository {
    override fun getAllSuggestions(): Flow<List<SupplierSuggestion>> = service.observeAllFrom(
        collectionName = "supplier-suggestions",
        type = SupplierSuggestion::class.java
    )

    override suspend fun addSuggestion(suggestion: SupplierSuggestion): Result<Unit> = service.putTo(
        collectionName = "supplier-suggestions",
        documentId = suggestion.suggestion,
        data = suggestion
    )

    override suspend fun updateSuggestion(suggestion: SupplierSuggestion): Result<Unit> = service.putTo(
        collectionName = "supplier-suggestions",
        documentId = suggestion.suggestion,
        data = suggestion
    )

    override suspend fun deleteSuggestion(id: String): Result<Unit> = service.deleteFrom(
        collectionName = "supplier-suggestions",
        documentId = id
    )
}