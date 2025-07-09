package kz.market.domain.models

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

data class UnitOption(
    val label: String,
    val value: String
)

val unitOptions: List<UnitOption> = listOf(
    UnitOption("Килограмм", "кг"),
    UnitOption("Штука", "шт"),
    UnitOption("Литр", "л")
)

@Keep
data class Product(
    val barcode: String = "",
    val name: String = "",
    val price: Double = 0.0,
    val ownPrice: Double = 0.0,
    val quantity: Int = 0,
    val unit: String = "",
    val createdAt: Timestamp = Timestamp.now(),
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

    fun isFullyEmpty(): Boolean = barcode.isEmpty() && name.isEmpty() && price == 0.0 && ownPrice == 0.0 && quantity == 0 && unit.isEmpty()

    fun isNotEmpty(): Boolean = !isFullyEmpty()

    fun hasAnyMissingFields(): Boolean = barcode.isEmpty() || name.isEmpty() || price == 0.0 || ownPrice == 0.0 || quantity == 0 || unit.isEmpty()

    fun isValid(): Boolean = !hasAnyMissingFields()

    companion object {
        val EMPTY = Product()
    }
}


@Keep
data class ProductInputState(
    val barcode: String = "",
    val name: String = "",
    val price: String = "",
    val ownPrice: String = "",
    val quantity: String = "",
    val unit: String = "",
    val createdAt: String = "",
    var updatedAt: String = ""
) {
    fun isValid(): Boolean =
            barcode.isNotBlank() &&
            name.isNotBlank() &&
            price.toDoubleOrNull()?.let { it > 0.0 } == true &&
            ownPrice.toDoubleOrNull()?.let { it > 0.0 } == true &&
            quantity.toIntOrNull()?.let { it > 0 } == true &&
            unit.isNotBlank()

    fun isNotValid(): Boolean = !isValid()

    fun toProduct(): Product = Product(
        barcode = barcode,
        name = name,
        price = price.toDoubleOrNull() ?: 0.0,
        ownPrice = ownPrice.toDoubleOrNull() ?: 0.0,
        quantity = quantity.toIntOrNull() ?: 0,
        unit = unit,
    )
}