package kz.market.domain.usecases.product

import kotlinx.coroutines.flow.Flow
import kz.market.domain.models.Product
import kz.market.domain.repository.ProductRepository
import javax.inject.Inject

class GetAllProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    operator fun invoke(): Flow<List<Product>> = productRepository.getAllProducts()
}