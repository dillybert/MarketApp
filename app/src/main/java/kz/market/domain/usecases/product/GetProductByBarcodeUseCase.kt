package kz.market.domain.usecases.product

import kz.market.domain.models.Product
import kz.market.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductByBarcodeUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(barcode: String): Result<Product?> = productRepository.getProductByBarcode(barcode)
}