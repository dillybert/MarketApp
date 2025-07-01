package kz.market.domain.usecases.product

import kz.market.domain.repository.ProductRepository
import javax.inject.Inject

class DeleteProductByBarcodeUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): Result<Unit> = productRepository.deleteProduct(barcode)
}