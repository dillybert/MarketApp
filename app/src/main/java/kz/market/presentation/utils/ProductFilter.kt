package kz.market.presentation.utils

sealed class ProductFilter(val displayName: String) {
    object All : ProductFilter("Все товары")
    object OutOfStock : ProductFilter("Остатки")
    object LastUpdated : ProductFilter("Последние обновления")
    object LastAdded : ProductFilter("Последние добавления")
    object Price : ProductFilter("По цене")

    companion object {
        fun getAll() = listOf(All, OutOfStock, LastUpdated, LastAdded, Price)
    }
}