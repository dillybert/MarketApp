package kz.market.domain.usecases.product

import kz.market.domain.models.Product
import kz.market.domain.repository.ProductRepository
import javax.inject.Inject

class AddProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product): Result<Unit> = productRepository.addProduct(product)
}