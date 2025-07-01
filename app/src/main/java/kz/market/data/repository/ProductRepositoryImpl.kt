package kz.market.data.repository

import kotlinx.coroutines.flow.Flow
import kz.market.data.remote.FirebaseProductService
import kz.market.domain.models.Product
import kz.market.domain.repository.ProductRepository
import javax.inject.Inject

class ProductRepositoryImpl @Inject constructor(
    private val service: FirebaseProductService
) : ProductRepository {
    override fun getAllProducts(): Flow<List<Product>> = service.observeAllFrom(
        collectionName = "products",
        clazz = Product::class.java
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

    override suspend fun getProductByBarcode(barcode: String): Product? = service.getById(
        collectionName = "products",
        documentId = barcode,
        clazz = Product::class.java
    )

    override suspend fun deleteProduct(barcode: String): Result<Unit> = service.deleteFrom(
        collectionName = "products",
        documentId = barcode
    )
}