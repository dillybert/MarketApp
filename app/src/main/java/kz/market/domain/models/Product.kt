package kz.market.domain.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

@Keep
data class Product(
    var barcode: String = "",
    var name: String = "",
    var price: Double = 0.0,
    var ownPrice: Double = 0.0,
    var supplier: String = "",
    var quantity: Double = 0.0,
    var unit: String = "",
    var createdAt: Timestamp = Timestamp.now(),
    var updatedAt: Timestamp = Timestamp.now()
) {
    val formattedPrice: String
        get() = String.format(
            locale = Locale.getDefault(),
            format = "%,.0f",
            price
        )

    val formattedCreatedAtDate: String
        get() = SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(createdAt.toDate())

    val formattedUpdatedAtDate: String
        get() = SimpleDateFormat(
            "dd.MM.yyyy HH:mm",
            Locale.getDefault()
        ).format(updatedAt.toDate())

    fun hasAnyMissingFields(): Boolean = barcode.isEmpty() || name.isEmpty() || price == 0.0 || ownPrice == 0.0 || supplier.isEmpty() || quantity == 0.0 || unit.isEmpty()

    companion object {
        val EMPTY = Product()
        const val DEFAULT_SUPPLIER = "Поставщик не указан"
    }
}