package kz.market.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kz.market.R
import kz.market.domain.models.Product

@Composable
fun ProductItem(
    product: Product,
    onClick: (product: Product) -> Unit,
    modifier: Modifier = Modifier,
    background: Color = MaterialTheme.colorScheme.surface
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable {
                onClick(product)
            }
            .background(background),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(start = 3.dp, end = 10.dp)
                .width(70.dp)
                .height(70.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.secondary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_package),
                tint = MaterialTheme.colorScheme.onSecondary,
                contentDescription = "Product image",
                modifier = Modifier
                    .width(30.dp)
                    .height(30.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(3f),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = product.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    text = "${if (product.quantity % 1 == 0.0) product.quantity.toInt() else product.quantity} ${product.unit}.",
                    style = MaterialTheme.typography.bodySmall
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = "Обновлено: ${product.formattedUpdatedAtDate}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }

        Row(
            modifier = Modifier
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                modifier = Modifier
                    .padding(bottom = 1.dp)
                    .width(13.dp)
                    .height(13.dp),
                painter = painterResource(R.drawable.ic_tenge_sign),
                contentDescription = "Product price"
            )
            Text(
                text = product.formattedPrice,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}

@Preview(
    showBackground = true
)
@Composable
private fun ProductItemPreview() {
    ProductItem(
        product = Product(
            barcode = "5555555555555",
            name = "Test Product",
            price = 21000.0,
            quantity = 100.0,
            unit = "шт"
        ),
        onClick = {}
    )
}