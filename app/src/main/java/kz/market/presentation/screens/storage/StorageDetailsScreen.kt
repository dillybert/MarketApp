package kz.market.presentation.screens.storage

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.Timestamp
import kz.market.R
import kz.market.domain.models.Product
import kz.market.domain.models.ProductInputState
import kz.market.domain.models.unitOptions
import kz.market.presentation.components.camera.CameraScannerSheet
import kz.market.utils.UIGetState
import kz.market.utils.UISetState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StorageDetailsContent(
    productState: UIGetState<Product>,
    updateProductResult: UISetState,
    deleteProductResult: UISetState,
    onProductSaveClick: (Product) -> Unit,
    onProductDeleteClick: (String) -> Unit,
    resetStates: () -> Unit
) {
    var showCameraScanner by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    var showDropDown by remember { mutableStateOf(false) }
    var showInputError by remember { mutableStateOf(false) }
    val snackBarHostState = remember { SnackbarHostState() }

    val inputState = remember(productState) { mutableStateOf(
        when (productState) {
            is UIGetState.Success<Product> -> productState.data.let {
                ProductInputState(
                    barcode = it.barcode,
                    name = it.name,
                    price = it.price.toString(),
                    ownPrice = it.ownPrice.toString(),
                    quantity = it.quantity.toString(),
                    unit = it.unit,
                    createdAt = it.formattedCreatedAtDate,
                    updatedAt = it.formattedUpdatedAtDate
                )
            }

            else -> ProductInputState()
        }
    ) }


    LaunchedEffect(updateProductResult) {
        when (updateProductResult) {
            is UISetState.Error -> {
                snackBarHostState.showSnackbar(
                    message = updateProductResult.message,
                    withDismissAction = true
                )
                resetStates()
            }

            is UISetState.Success -> {
                snackBarHostState.showSnackbar(
                    message = "Товар успешно обновлен",
                    withDismissAction = true
                )
                resetStates()
            }

            else -> Unit
        }
    }

    LaunchedEffect(deleteProductResult) {
        when (deleteProductResult) {
            is UISetState.Error -> {
                snackBarHostState.showSnackbar(
                    message = deleteProductResult.message,
                    withDismissAction = true
                )
                resetStates()
            }

            is UISetState.Success -> {
                snackBarHostState.showSnackbar(
                    message = "Товар успешно удален",
                    withDismissAction = true
                )
                resetStates()
            }

            else -> Unit
        }
    }


    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Детализация товара")
                }
            )
        },
        snackbarHost = {
            SnackbarHost(hostState = snackBarHostState)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (productState) {
                is UIGetState.Empty -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                modifier = Modifier
                                    .fillMaxWidth(.3f)
                                    .aspectRatio(1f),
                                painter = painterResource(R.drawable.ic_info),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                contentDescription = "Info"
                            )

                            Text(
                                modifier = Modifier
                                    .padding(top = 20.dp, bottom = 8.dp),
                                text = "Информация",
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.titleLarge
                            )

                            Text(
                                text = "К сожалению, товар не найден. Возможно, он был удалён.",
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                is UIGetState.Error -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .fillMaxWidth(.3f)
                                        .aspectRatio(1f),
                                    painter = painterResource(R.drawable.ic_x_circle),
                                    tint = MaterialTheme.colorScheme.error,
                                    contentDescription = "Error"
                                )

                                Spacer(modifier = Modifier.height(20.dp))

                                Text(
                                    text = "Ошибка при загрузке данных",
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.titleLarge
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = productState.message,
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }

                is UIGetState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                is UIGetState.Success<Product> -> {
                    LazyColumn(
                        modifier = Modifier
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(20.dp),
                    ) {
                        item {

                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(10.dp)
                            ) {
                                Icon(
                                    modifier = Modifier
                                        .padding(top = 3.dp, end = 10.dp),
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = "Info",
                                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )

                                Text(
                                    text = """ Перед обновлением данных товара, пожалуйста, убедитесь в следующем:
    • Название товара не пустое
    • Штрих-код введён корректно
    • Цена больше 0 (в тенге)
    • Количество больше 0
                    """.trimIndent(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                                )

                            }
                        }

                        item {
                            val selectedLabel = unitOptions.find { it.value == inputState.value.unit }?.label
                                ?: "Выберите единицу измерения"

                            OutlinedTextField(
                                isError = showInputError,
                                singleLine = true,
                                supportingText = {
                                    if (showInputError) {
                                        Text(
                                            text = "Это поле обязательно для заполнения",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                value = inputState.value.barcode,
                                onValueChange = { inputState.value = inputState.value.copy(barcode = it) },
                                label = { Text("Штрих-код товара") },
                                trailingIcon = {
                                    IconButton(
                                        onClick = {
                                            showCameraScanner = true
                                        }
                                    ) {
                                        Icon(
                                            painter = painterResource(R.drawable.ic_scan),
                                            contentDescription = "Barcode"
                                        )
                                    }
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )


                            OutlinedTextField(
                                isError = showInputError,
                                singleLine = true,
                                supportingText = {
                                    if (showInputError) {
                                        Text(
                                            text = "Это поле обязательно для заполнения",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                value = inputState.value.name,
                                onValueChange = { inputState.value = inputState.value.copy(name = it) },
                                label = { Text("Название товара") }
                            )


                            OutlinedTextField(
                                isError = showInputError,
                                singleLine = true,
                                supportingText = {
                                    if (showInputError) {
                                        Text(
                                            text = "Это поле обязательно для заполнения",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                value = inputState.value.price,
                                onValueChange = {
                                    if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                                        inputState.value = inputState.value.copy(price = it)
                                    }
                                },
                                trailingIcon = {
                                    Icon(
                                        modifier = Modifier
                                            .width(15.dp)
                                            .height(15.dp),
                                        painter = painterResource(R.drawable.ic_tenge_sign),
                                        contentDescription = "Product price"
                                    )
                                },
                                label = { Text("Реализационная цена товара") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )


                            OutlinedTextField(
                                isError = showInputError,
                                singleLine = true,
                                supportingText = {
                                    if (showInputError) {
                                        Text(
                                            text = "Это поле обязательно для заполнения",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                value = inputState.value.ownPrice,
                                onValueChange = {
                                    if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                                        inputState.value = inputState.value.copy(ownPrice = it)
                                    }
                                },
                                trailingIcon = {
                                    Icon(
                                        modifier = Modifier
                                            .width(15.dp)
                                            .height(15.dp),
                                        painter = painterResource(R.drawable.ic_tenge_sign),
                                        contentDescription = "Product price"
                                    )
                                },
                                label = { Text("Себестоимость товара") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )



                            ExposedDropdownMenuBox(
                                expanded = showDropDown,
                                onExpandedChange = { showDropDown = !showDropDown }
                            ) {
                                OutlinedTextField(
                                    readOnly = true,
                                    value = selectedLabel,
                                    onValueChange = {},
                                    label = {
                                        Text("Единица измерения товара")
                                    },
                                    trailingIcon = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(showDropDown)
                                    },
                                    modifier = Modifier
                                        .menuAnchor(type = MenuAnchorType.PrimaryNotEditable)
                                        .fillMaxWidth(),
                                )

                                ExposedDropdownMenu(
                                    expanded = showDropDown,
                                    onDismissRequest = { showDropDown = false }
                                ) {
                                    unitOptions.forEach { (label, value) ->
                                        DropdownMenuItem(
                                            text = { Text(label) },
                                            onClick = {
                                                inputState.value = inputState.value.copy(unit = value)
                                                showDropDown = false
                                            }
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(15.dp))

                            OutlinedTextField(
                                isError = showInputError,
                                singleLine = true,
                                supportingText = {
                                    if (showInputError) {
                                        Text(
                                            text = "Это поле обязательно для заполнения",
                                            color = MaterialTheme.colorScheme.error,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth(),
                                value = inputState.value.quantity,
                                onValueChange = {
                                    if (it.all { ch -> ch.isDigit() || ch == '.' }) {
                                        inputState.value = inputState.value.copy(quantity = it)
                                    }
                                },
                                label = { Text("Остаток товара на складе") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            Text(
                                modifier = Modifier
                                    .padding(bottom = 15.dp),
                                text = "Товар создан ${inputState.value.createdAt}",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Text(
                                text = "Последнее обновление ${inputState.value.updatedAt}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        item {
                            OutlinedButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                onClick = {
                                    if (inputState.value.isNotValid()) {
                                        showInputError = true
                                        return@OutlinedButton
                                    }

                                    showInputError = false

                                    val product = inputState.value.toProduct()
                                    product.updatedAt = Timestamp.now()

                                    onProductSaveClick(product)
                                }
                            ) {
                                Text(
                                    text = "Обновить данные товара"
                                )
                            }
                        }

                        item {
                            OutlinedButton(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                ),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                                onClick = {
                                    showDialog = true
                                }
                            ) {
                                Text(
                                    text = "Удалить товар"
                                )
                            }
                        }
                    }
                }
            }
        }


        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Удаление товара") },
                text = { Text("Вы уверены, что хотите удалить этот товар? Это действие нельзя будет отменить.") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false

                        onProductDeleteClick(inputState.value.barcode)
                    }) {
                        Text("Удалить", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Отмена")
                    }
                }
            )
        }

        CameraScannerSheet(
            visible = showCameraScanner,
            onDismiss = { showCameraScanner = false },
            onScan = { barcode ->
                inputState.value = inputState.value.copy(barcode = barcode)
                showCameraScanner = false
            }
        )
    }
}


@Composable
fun StorageDetailsScreen(
    viewModel: StorageViewModel = hiltViewModel(),
    barcode: String
) {
    LaunchedEffect(barcode) {
        viewModel.getProductByBarcode(barcode)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    val productState by viewModel.productState.collectAsState()
    val updateProductResult by viewModel.updateProductResult.collectAsState()
    val deleteProductResult by viewModel.deleteProductResult.collectAsState()

    StorageDetailsContent(
        productState = productState,
        updateProductResult = updateProductResult,
        deleteProductResult = deleteProductResult,
        onProductSaveClick = viewModel::updateProduct,
        onProductDeleteClick = viewModel::deleteProduct,
        resetStates = viewModel::resetState
    )
}


@Preview(
    showBackground = true,
    showSystemUi = true,
    device = "spec:width=411dp,height=891dp"
)
@Composable
private fun StorageDetailsScreenPreview() {
    StorageDetailsContent(
        productState = UIGetState.Success(
            Product()
        ),
        updateProductResult = UISetState.Idle,
        deleteProductResult = UISetState.Idle,
        onProductSaveClick = {},
        onProductDeleteClick = {},
        resetStates = {}
    )
}