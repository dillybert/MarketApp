package kz.market.data.repository

import kotlinx.coroutines.flow.Flow
import kz.market.data.remote.FirebaseFirestoreService
import kz.market.domain.models.Product
import kz.market.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val service: FirebaseFirestoreService
) : ProductRepository {

    override fun getAllProducts(): Flow<List<Product>> = service.observeAllFrom(
        collectionName = "products",
        type = Product::class.java
    )

    override suspend fun addProduct(product: Product): Result<Unit> = service.putTo(
        collectionName = "products",
        documentId = product.barcode,
        data = product
    )

    override suspend fun updateProduct(product: Product): Result<Unit> = service.putTo(
        collectionName = "products",
        documentId = product.barcode,
        data = product
    )

    override suspend fun getProductByBarcode(barcode: String): Result<Product?> = service.getById(
        collectionName = "products",
        documentId = barcode,
        type = Product::class.java
    )

    override suspend fun deleteProduct(barcode: String): Result<Unit> = service.deleteFrom(
        collectionName = "products",
        documentId = barcode
    )

}