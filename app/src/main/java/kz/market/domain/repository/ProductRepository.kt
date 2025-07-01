package kz.market.domain.repository

import kotlinx.coroutines.flow.Flow
import kz.market.domain.models.Product

interface ProductRepository {
    fun getAllProducts(): Flow<List<Product>>
    suspend fun getProductByBarcode(barcode: String): Product?
    suspend fun addProduct(product: Product): Result<Unit>
    suspend fun updateProduct(product: Product): Result<Unit>
    suspend fun deleteProduct(barcode: String): Result<Unit>
}